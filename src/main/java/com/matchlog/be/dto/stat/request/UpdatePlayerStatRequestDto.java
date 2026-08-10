package com.matchlog.be.dto.stat.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePlayerStatRequestDto {

    @Builder.Default private int goals = 0;

    @Builder.Default private int assists = 0;

    @Builder.Default private int shots = 0;

    private Integer saves;
    private Integer goalsConceded;
    private Boolean cleanSheet;
}
