package com.matchlog.be.service;

import com.matchlog.be.constant.user.Provider;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.dto.auth.request.LoginRequestDto;
import com.matchlog.be.dto.auth.request.ReissueRequestDto;
import com.matchlog.be.dto.auth.request.SignupRequestDto;
import com.matchlog.be.dto.auth.response.CheckEmailResponseDto;
import com.matchlog.be.dto.auth.response.LoginResponseDto;
import com.matchlog.be.dto.auth.response.ReissueResponseDto;
import com.matchlog.be.dto.auth.response.SignupResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.AuthErrorCode;
import com.matchlog.be.external.oauth.OAuthUserInfo;
import com.matchlog.be.external.oauth.google.GoogleOAuthClient;
import com.matchlog.be.external.oauth.kakao.KakaoOAuthClient;
import com.matchlog.be.repository.UserRepository;
import com.matchlog.be.util.jwt.JwtTokenProvider;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final GoogleOAuthClient googleOAuthClient;

    @Transactional
    public SignupResponseDto signup(SignupRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException(AuthErrorCode.EMAIL_CONFLICT);
        }
        User user =
                User.create(
                        dto.getEmail(),
                        passwordEncoder.encode(dto.getPassword()),
                        dto.getName(),
                        dto.getProfileImage(),
                        Provider.LOCAL);
        userRepository.save(user);
        return SignupResponseDto.from(user);
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto dto) {
        User user =
                userRepository
                        .findByEmail(dto.getEmail())
                        .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_CREDENTIALS));

        if (user.getProvider() != Provider.LOCAL) {
            throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        return issueTokens(user);
    }

    @Transactional
    public LoginResponseDto oauthLogin(Provider provider, String code) {
        OAuthUserInfo info =
                switch (provider) {
                    case KAKAO -> kakaoOAuthClient.getUserInfo(code);
                    case GOOGLE -> googleOAuthClient.getUserInfo(code);
                    default -> throw new CustomException(AuthErrorCode.INVALID_OAUTH_CODE);
                };

        userRepository
                .findByEmail(info.email())
                .ifPresent(
                        existing -> {
                            if (existing.getProvider() != provider) {
                                throw new CustomException(AuthErrorCode.PROVIDER_MISMATCH);
                            }
                        });

        User user =
                userRepository
                        .findByEmailAndProvider(info.email(), provider)
                        .orElseGet(
                                () ->
                                        userRepository.save(
                                                User.create(
                                                        info.email(),
                                                        null,
                                                        info.name(),
                                                        info.profileImage(),
                                                        provider)));
        return issueTokens(user);
    }

    @Transactional
    public ReissueResponseDto reissue(ReissueRequestDto dto) {
        jwtTokenProvider.validateOrThrow(dto.getRefreshToken());
        Long userId = jwtTokenProvider.getUserId(dto.getRefreshToken());

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if (!dto.getRefreshToken().equals(user.getRefreshToken())) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccess = jwtTokenProvider.createAccessToken(userId);
        String newRefresh = jwtTokenProvider.createRefreshToken(userId);
        user.updateRefreshToken(newRefresh);

        return ReissueResponseDto.builder().accessToken(newAccess).refreshToken(newRefresh).build();
    }

    @Transactional
    public void logout(Long userId, String accessToken) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN));
        user.clearRefreshToken();

        long remainingExpiry = jwtTokenProvider.getRemainingExpiry(accessToken);
        if (remainingExpiry > 0) {
            redisTemplate
                    .opsForValue()
                    .set(BLACKLIST_PREFIX + accessToken, "1", Duration.ofMillis(remainingExpiry));
        }
    }

    @Transactional(readOnly = true)
    public CheckEmailResponseDto checkEmail(String email) {
        return CheckEmailResponseDto.builder()
                .available(!userRepository.existsByEmail(email))
                .build();
    }

    private LoginResponseDto issueTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        user.updateRefreshToken(refreshToken);
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .build();
    }
}
