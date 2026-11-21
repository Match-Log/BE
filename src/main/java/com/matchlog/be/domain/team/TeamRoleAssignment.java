package com.matchlog.be.domain.team;

import com.matchlog.be.domain.common.BaseTimeEntity;
import com.matchlog.be.domain.participation.Participation;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "TEAM_ROLE_ASSIGNMENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TeamRoleAssignment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teamId", nullable = false, unique = true)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captainParticipationId")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Participation captainParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pkTakerParticipationId")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Participation pkTakerParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ckLeftParticipationId")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Participation ckLeftParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ckRightParticipationId")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Participation ckRightParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fkLeftParticipationId")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Participation fkLeftParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fkRightParticipationId")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Participation fkRightParticipation;

    public static TeamRoleAssignment create(Team team) {
        return TeamRoleAssignment.builder().team(team).build();
    }

    /** 탈퇴/제외 선수의 세트피스 역할 슬롯을 모두 해제한다. LAZY 프록시에서 getId()는 DB 조회 없이 반환됨. */
    public void clearSlotsForParticipation(Long participationId) {
        if (captainParticipation != null && participationId.equals(captainParticipation.getId()))
            captainParticipation = null;
        if (pkTakerParticipation != null && participationId.equals(pkTakerParticipation.getId()))
            pkTakerParticipation = null;
        if (ckLeftParticipation != null && participationId.equals(ckLeftParticipation.getId()))
            ckLeftParticipation = null;
        if (ckRightParticipation != null && participationId.equals(ckRightParticipation.getId()))
            ckRightParticipation = null;
        if (fkLeftParticipation != null && participationId.equals(fkLeftParticipation.getId()))
            fkLeftParticipation = null;
        if (fkRightParticipation != null && participationId.equals(fkRightParticipation.getId()))
            fkRightParticipation = null;
    }

    /** PUT 시맨틱: 전달된 슬롯 그대로 덮어씀(null이면 해당 역할 해제). */
    public void updateRoles(
            Participation captainParticipation,
            Participation pkTakerParticipation,
            Participation ckLeftParticipation,
            Participation ckRightParticipation,
            Participation fkLeftParticipation,
            Participation fkRightParticipation) {
        this.captainParticipation = captainParticipation;
        this.pkTakerParticipation = pkTakerParticipation;
        this.ckLeftParticipation = ckLeftParticipation;
        this.ckRightParticipation = ckRightParticipation;
        this.fkLeftParticipation = fkLeftParticipation;
        this.fkRightParticipation = fkRightParticipation;
    }
}
