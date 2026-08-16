package com.matchlog.be.dto.auth.request;

import com.matchlog.be.constant.user.Provider;
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
    private Provider provider;
}
