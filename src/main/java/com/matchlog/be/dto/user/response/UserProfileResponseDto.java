package com.matchlog.be.dto.user.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.constant.user.Provider;
import com.matchlog.be.domain.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponseDto {

    private Long userId;
    private String email;
    private String name;
    private String profileImage;
    private Provider provider;
    private Long playerId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static UserProfileResponseDto from(User user, Long playerId) {
        return UserProfileResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .provider(user.getProvider())
                .playerId(playerId)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
