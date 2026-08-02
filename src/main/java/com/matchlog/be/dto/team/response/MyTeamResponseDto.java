package com.matchlog.be.dto.team.response;

import com.matchlog.be.domain.team.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyTeamResponseDto {

    private Long teamId;
    private String name;
    private String teamImage;
    private String region;
    private Integer foundedYear;
    private String homeGround;
    private String role;
    private String inviteCode;

    public static MyTeamResponseDto from(Team team, String role) {
        return MyTeamResponseDto.builder()
                .teamId(team.getId())
                .name(team.getName())
                .teamImage(team.getTeamImage())
                .region(team.getRegion())
                .foundedYear(team.getFoundedYear())
                .homeGround(team.getHomeGround())
                .role(role)
                .inviteCode(team.getInviteCode())
                .build();
    }
}
