package com.matchlog.be.event;

import com.matchlog.be.constant.notification.NotificationType;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.repository.UserRepository;
import com.matchlog.be.service.notification.NotificationService;
import com.matchlog.be.util.KoreanParticleUtil;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMatchCreated(MatchCreatedEvent event) {
        List<User> users = userRepository.findAllById(event.memberUserIds());
        String title = "새 경기 등록";
        String body =
                event.matchDate().format(DateTimeFormatter.ofPattern("M/d HH:mm"))
                        + " "
                        + event.opponent()
                        + KoreanParticleUtil.select(event.opponent(), "과", "와")
                        + "의 경기가 등록됐습니다.";
        users.forEach(
                user ->
                        notificationService.create(
                                user,
                                NotificationType.MATCH_CREATED,
                                title,
                                body,
                                event.matchId(),
                                event.teamId()));
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPlayerJoined(PlayerJoinedTeamEvent event) {
        List<User> users = userRepository.findAllById(event.existingMemberUserIds());
        String title = "새 팀원 합류";
        String body = event.teamName() + " 팀에 " + event.newPlayerName() + " 선수가 합류했습니다.";
        users.forEach(
                user ->
                        notificationService.create(
                                user,
                                NotificationType.PLAYER_JOINED,
                                title,
                                body,
                                event.teamId(),
                                event.teamId()));
    }
}
