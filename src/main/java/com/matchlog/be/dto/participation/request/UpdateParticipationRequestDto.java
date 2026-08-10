package com.matchlog.be.dto.participation.request;

import com.matchlog.be.constant.participation.ParticipationRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateParticipationRequestDto {

    private ParticipationRole role;
    private Integer number;
    private String mainPosition;
    private String subPosition;
}
