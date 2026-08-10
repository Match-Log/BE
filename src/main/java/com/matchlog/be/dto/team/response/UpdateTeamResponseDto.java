package com.matchlog.be.dto.team.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.domain.team.Team;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTeamResponseDto {

    private Long teamId;
    private String name;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static UpdateTeamResponseDto from(Team team) {
        return UpdateTeamResponseDto.builder()
                .teamId(team.getId())
                .name(team.getName())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
}
