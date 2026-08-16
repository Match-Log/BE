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
public class TeamResponseDto {

    private Long teamId;
    private String name;
    private String teamImage;
    private String region;
    private Integer foundedYear;
    private String homeGround;
    private String inviteCode;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static TeamResponseDto from(Team team) {
        return TeamResponseDto.builder()
                .teamId(team.getId())
                .name(team.getName())
                .teamImage(team.getTeamImage())
                .region(team.getRegion())
                .foundedYear(team.getFoundedYear())
                .homeGround(team.getHomeGround())
                .inviteCode(team.getInviteCode())
                .createdAt(team.getCreatedAt())
                .build();
    }
}
