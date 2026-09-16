package com.matchlog.be.dto.team.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.team.TeamRoleAssignment;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleAssignmentResponseDto {

    private Long teamId;
    private RoleSlot captain;
    private RoleSlot pkTaker;
    private RoleSlot ckLeft;
    private RoleSlot ckRight;
    private RoleSlot fkLeft;
    private RoleSlot fkRight;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static RoleAssignmentResponseDto from(TeamRoleAssignment assignment) {
        return RoleAssignmentResponseDto.builder()
                .teamId(assignment.getTeam().getId())
                .captain(RoleSlot.from(assignment.getCaptainParticipation()))
                .pkTaker(RoleSlot.from(assignment.getPkTakerParticipation()))
                .ckLeft(RoleSlot.from(assignment.getCkLeftParticipation()))
                .ckRight(RoleSlot.from(assignment.getCkRightParticipation()))
                .fkLeft(RoleSlot.from(assignment.getFkLeftParticipation()))
                .fkRight(RoleSlot.from(assignment.getFkRightParticipation()))
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleSlot {

        private Long participationId;
        private Long playerId;
        private String name;

        private static RoleSlot from(Participation participation) {
            if (participation == null) {
                return null;
            }
            return RoleSlot.builder()
                    .participationId(participation.getId())
                    .playerId(participation.getPlayer().getId())
                    .name(participation.getPlayer().getUser().getName())
                    .build();
        }
    }
}
