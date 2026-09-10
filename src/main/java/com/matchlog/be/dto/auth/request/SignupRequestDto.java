package com.matchlog.be.dto.auth.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDto {

    private String email;
    private String password;
    private String name;
    private String profileImage;
}
