package com.matchlog.be.dto.lineup.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.domain.match.MatchGuest;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchGuestResponseDto {

    private Long id;
    private Long matchId;
    private String name;
    private String position;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static MatchGuestResponseDto from(MatchGuest guest) {
        return MatchGuestResponseDto.builder()
                .id(guest.getId())
                .matchId(guest.getMatch().getId())
                .name(guest.getName())
                .position(guest.getPosition())
                .createdAt(guest.getCreatedAt())
                .build();
    }
}
