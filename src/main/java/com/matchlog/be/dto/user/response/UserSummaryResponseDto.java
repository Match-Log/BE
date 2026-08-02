package com.matchlog.be.dto.user.response;

import com.matchlog.be.domain.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryResponseDto {

    private Long userId;
    private String name;
    private String profileImage;
    private Long playerId;

    public static UserSummaryResponseDto from(User user, Long playerId) {
        return UserSummaryResponseDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .playerId(playerId)
                .build();
    }
}
