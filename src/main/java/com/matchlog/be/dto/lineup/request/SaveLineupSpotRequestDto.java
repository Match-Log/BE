package com.matchlog.be.dto.lineup.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveLineupSpotRequestDto {

    @NotNull private Long playerId;

    @NotBlank private String position;

    private boolean isStarter;
}
