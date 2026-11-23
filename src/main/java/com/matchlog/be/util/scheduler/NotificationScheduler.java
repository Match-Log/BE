package com.matchlog.be.util.scheduler;

import com.matchlog.be.constant.notification.NotificationType;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.NotificationRepository;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.service.notification.NotificationService;
import com.matchlog.be.util.KoreanParticleUtil;
import com.matchlog.be.util.notification.SseEmitterManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final MatchRepository matchRepository;
    private final ParticipationRepository participationRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final SseEmitterManager sseEmitterManager;

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void notifyTodayMatch() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<Match> matches = matchRepository.findMatchesWithTeamBetween(start, end);

        for (Match match : matches) {
            if (notificationRepository.existsByReferenceIdAndType(
                    match.getId(), NotificationType.TODAY_MATCH)) {
                continue;
            }
            String title = "오늘 경기 알림";
            String body =
                    match.getMatchDate().toLocalTime()
                            + "에 "
                            + match.getOpponent()
                            + KoreanParticleUtil.select(match.getOpponent(), "과", "와")
                            + " 경기가 있습니다.";
            sendToRoster(match, NotificationType.TODAY_MATCH, title, body);
        }
    }

    @Scheduled(fixedDelay = 600_000)
    @Transactional
    public void notifyMatchReviewReminder() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);
        LocalDateTime lookback = threshold.minusHours(48);
        List<Match> candidates = matchRepository.findMatchesWithTeamForReview(lookback, threshold);

        for (Match match : candidates) {
            if (notificationRepository.existsByReferenceIdAndType(
                    match.getId(), NotificationType.MATCH_REVIEW_REMINDER)) {
                continue;
            }
            String title = "경기 피드백 작성";
            String body =
                    match.getOpponent()
                            + KoreanParticleUtil.select(match.getOpponent(), "과", "와")
                            + "의 경기가 종료됐습니다. 경기기록을 입력해주세요.";
            sendToRoster(match, NotificationType.MATCH_REVIEW_REMINDER, title, body);
        }
    }

    @Scheduled(fixedDelay = 30_000)
    public void sendHeartbeat() {
        sseEmitterManager.sendHeartbeat();
    }

    private void sendToRoster(Match match, NotificationType type, String title, String body) {
        List<Participation> roster =
                participationRepository.findRosterByTeamId(match.getTeam().getId());
        roster.forEach(
                p ->
                        notificationService.create(
                                p.getPlayer().getUser(),
                                type,
                                title,
                                body,
                                match.getId(),
                                match.getTeam().getId()));
    }
}
