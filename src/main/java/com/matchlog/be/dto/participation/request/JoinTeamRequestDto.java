package com.matchlog.be.dto.participation.request;

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
public class JoinTeamRequestDto {

    @NotBlank
    @Size(min = 6, max = 6)
    private String inviteCode;
}
