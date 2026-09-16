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
    private Participation captainParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pkTakerParticipationId")
    private Participation pkTakerParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ckLeftParticipationId")
    private Participation ckLeftParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ckRightParticipationId")
    private Participation ckRightParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fkLeftParticipationId")
    private Participation fkLeftParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fkRightParticipationId")
    private Participation fkRightParticipation;

    public static TeamRoleAssignment create(Team team) {
        return TeamRoleAssignment.builder().team(team).build();
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
