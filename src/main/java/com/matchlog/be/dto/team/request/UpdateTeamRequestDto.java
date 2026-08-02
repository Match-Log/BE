package com.matchlog.be.dto.team.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTeamRequestDto {

    private String name;
    private String teamImage;
    private String region;
    private Integer foundedYear;
    private String homeGround;
}
