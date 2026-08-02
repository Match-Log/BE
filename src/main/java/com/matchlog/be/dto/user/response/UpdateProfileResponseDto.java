package com.matchlog.be.dto.user.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.domain.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileResponseDto {

    private Long userId;
    private String name;
    private String profileImage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static UpdateProfileResponseDto from(User user) {
        return UpdateProfileResponseDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
