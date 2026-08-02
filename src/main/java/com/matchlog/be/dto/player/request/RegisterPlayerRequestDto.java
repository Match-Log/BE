package com.matchlog.be.dto.player.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.constant.player.PreferredFoot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterPlayerRequestDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private Integer height;
    private Integer weight;
    private PreferredFoot preferredFoot;
    private String career;
}
