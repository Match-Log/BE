package com.matchlog.be.dto.participation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoinTeamResponseDto {

    private Long participationId;
    private Long teamId;
    private String teamName;
    private String teamImage;
    private Long playerId;
    private ParticipationRole role;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime joinedAt;

    public static JoinTeamResponseDto from(Participation participation) {
        return JoinTeamResponseDto.builder()
                .participationId(participation.getId())
                .teamId(participation.getTeam().getId())
                .teamName(participation.getTeam().getName())
                .teamImage(participation.getTeam().getTeamImage())
                .playerId(participation.getPlayer().getId())
                .role(participation.getRole())
                .joinedAt(participation.getJoinedAt())
                .build();
    }
}
