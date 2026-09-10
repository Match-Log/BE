package com.matchlog.be.dto.lineup.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMatchGuestRequestDto {

    @NotBlank private String name;

    private String position;
}
