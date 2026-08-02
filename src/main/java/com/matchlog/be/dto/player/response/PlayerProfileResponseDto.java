package com.matchlog.be.dto.player.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.constant.player.PreferredFoot;
import com.matchlog.be.domain.player.Player;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerProfileResponseDto {

    private Long playerId;
    private Long userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private Integer height;
    private Integer weight;
    private PreferredFoot preferredFoot;
    private String career;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static PlayerProfileResponseDto from(Player player) {
        return PlayerProfileResponseDto.builder()
                .playerId(player.getId())
                .userId(player.getUser().getId())
                .birthDate(player.getBirthDate())
                .height(player.getHeight())
                .weight(player.getWeight())
                .preferredFoot(player.getPreferredFoot())
                .career(player.getCareer())
                .createdAt(player.getCreatedAt())
                .build();
    }
}
