package com.matchlog.be.dto.team.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRoleAssignmentRequestDto {

    private Long captainParticipationId;
    private Long pkTakerParticipationId;
    private Long ckLeftParticipationId;
    private Long ckRightParticipationId;
    private Long fkLeftParticipationId;
    private Long fkRightParticipationId;
}
