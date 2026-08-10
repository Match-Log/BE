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
public class RosterItemResponseDto {

    private Long participationId;
    private Long teamId;
    private Long playerId;
    private Long userId;
    private String name;
    private String profileImage;
    private ParticipationRole role;
    private Integer number;
    private String mainPosition;
    private String subPosition;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime joinedAt;

    public static RosterItemResponseDto from(Participation participation) {
        return RosterItemResponseDto.builder()
                .participationId(participation.getId())
                .teamId(participation.getTeam().getId())
                .playerId(participation.getPlayer().getId())
                .userId(participation.getPlayer().getUser().getId())
                .name(participation.getPlayer().getUser().getName())
                .profileImage(participation.getPlayer().getUser().getProfileImage())
                .role(participation.getRole())
                .number(participation.getNumber())
                .mainPosition(participation.getMainPosition())
                .subPosition(participation.getSubPosition())
                .joinedAt(participation.getJoinedAt())
                .build();
    }
}
