package com.matchlog.be.controller;

import com.matchlog.be.constant.user.Provider;
import com.matchlog.be.dto.auth.request.LoginRequestDto;
import com.matchlog.be.dto.auth.request.OAuthLoginRequestDto;
import com.matchlog.be.dto.auth.request.ReissueRequestDto;
import com.matchlog.be.dto.auth.request.SignupRequestDto;
import com.matchlog.be.dto.auth.response.CheckEmailResponseDto;
import com.matchlog.be.dto.auth.response.LoginResponseDto;
import com.matchlog.be.dto.auth.response.ReissueResponseDto;
import com.matchlog.be.dto.auth.response.SignupResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.AuthErrorCode;
import com.matchlog.be.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponseDto signup(@Valid @RequestBody SignupRequestDto dto) {
        return authService.signup(dto);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto dto) {
        return authService.login(dto);
    }

    @PostMapping("/oauth/{provider}")
    public LoginResponseDto oauthLogin(
            @PathVariable String provider, @Valid @RequestBody OAuthLoginRequestDto dto) {
        Provider p;
        try {
            p = Provider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(AuthErrorCode.INVALID_OAUTH_CODE);
        }
        return authService.oauthLogin(p, dto.getCode());
    }

    @PostMapping("/reissue")
    public ReissueResponseDto reissue(@Valid @RequestBody ReissueRequestDto dto) {
        return authService.reissue(dto);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @AuthenticationPrincipal Long userId,
            @RequestHeader("Authorization") String authorization) {
        String accessToken = authorization.substring("Bearer ".length());
        authService.logout(userId, accessToken);
    }

    @GetMapping("/check-email")
    public CheckEmailResponseDto checkEmail(@RequestParam String email) {
        return authService.checkEmail(email);
    }
}
