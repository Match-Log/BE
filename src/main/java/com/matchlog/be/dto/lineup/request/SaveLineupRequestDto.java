package com.matchlog.be.dto.lineup.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveLineupRequestDto {

    private Long matchId;

    @Min(1)
    @Max(4)
    private int quarter;

    @NotBlank private String formation;

    @NotEmpty @Valid private List<SaveLineupSpotRequestDto> spots;
}
