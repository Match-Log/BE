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

    private Integer goals;

    private Integer assists;

    private Integer saves;
}
