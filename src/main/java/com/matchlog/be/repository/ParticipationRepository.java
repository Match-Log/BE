package com.matchlog.be.repository;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    // [PATCH|DELETE /api/v1/teams/{teamId}/players/{playerId}] 참가 정보 수정/제외 시 해당 멤버 조회
    Optional<Participation> findByTeam_IdAndPlayer_Id(Long teamId, Long playerId);

    // [POST /api/v1/teams/join] 팀 중복 가입 체크 (UX용 선체크 — DB 유니크 제약이 최종 안전장치)
    boolean existsByTeam_IdAndPlayer_Id(Long teamId, Long playerId);

    // [POST /api/v1/teams/join] 팀 2개 제한 초과 여부 체크
    long countByPlayer_Id(Long playerId);

    // [DELETE /api/v1/teams/{teamId}/players/{playerId}] 마지막 MANAGER 퇴출 방지 체크
    long countByTeam_IdAndRole(Long teamId, ParticipationRole role);

    // [GET /api/v1/teams/{teamId}/players] 팀 로스터 조회
    // 규칙§3: Participation → Player → User 3단계 fetch join.
    @Query(
            "SELECT p FROM Participation p JOIN FETCH p.player pl JOIN FETCH pl.user WHERE p.team.id = :teamId")
    List<Participation> findRosterByTeamId(@Param("teamId") Long teamId);

    // [GET /api/v1/teams] 내 팀 목록 — 해당 선수가 속한 팀 + role 반환
    // 규칙§3: Participation → Team 2단계, 목록 조회이므로 fetch join 필수.
    @Query("SELECT p FROM Participation p JOIN FETCH p.team WHERE p.player.id = :playerId")
    List<Participation> findMyTeamsByPlayerId(@Param("playerId") Long playerId);

    // [PUT /api/v1/teams/{teamId}/players/{playerId}/kicker] 주장 중복 체크
    // boolean isCaptain의 JavaBeans 프로퍼티명(captain)과 혼동 방지를 위해 @Query 명시.
    // existsBy 대신 Optional: 기존 주장 해제(update)가 필요할 수 있어 엔티티 반환.
    @Query("SELECT p FROM Participation p WHERE p.team.id = :teamId AND p.isCaptain = true")
    Optional<Participation> findCurrentCaptainByTeamId(@Param("teamId") Long teamId);

    // [PUT /api/v1/teams/{teamId}/players/{playerId}/kicker] PK 키커 중복 체크
    // isPkTaker 동일 이유.
    @Query("SELECT p FROM Participation p WHERE p.team.id = :teamId AND p.isPkTaker = true")
    Optional<Participation> findCurrentPkTakerByTeamId(@Param("teamId") Long teamId);
}
