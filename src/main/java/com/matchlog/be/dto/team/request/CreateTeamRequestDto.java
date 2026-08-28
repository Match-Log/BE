package com.matchlog.be.dto.team.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTeamRequestDto {

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    private String teamImage;
    private String region;
    private Integer foundedYear;
    private String homeGround;
}
