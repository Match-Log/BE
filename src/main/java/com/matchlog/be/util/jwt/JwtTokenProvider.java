package com.matchlog.be.util.jwt;

import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.AuthErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰을 생성·검증·파싱하는 핵심 유틸리티.
 *
 * <p>역할 요약: "토큰 공장 + 토큰 검사관"
 *
 * <ul>
 *   <li>로그인 성공 시 → AccessToken / RefreshToken 생성
 *   <li>요청이 들어올 때마다 → 토큰이 유효한지 검증
 *   <li>토큰에서 userId 꺼내기
 * </ul>
 *
 * gangku 대응 파일: util/jwt/JwtTokenProvider.java
 * 차이점: gangku는 토큰에 "type":"access" 클레임을 넣어 access/refresh를 구분하고,
 *        이 파일은 메서드(validate vs validateOrThrow)로 두 용도를 구분함.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    /**
     * 생성자 — application.yml의 jwt.* 값을 주입받아 서명 키와 만료 시간을 초기화.
     * @Value로 환경변수가 자동 바인딩됨. (.env → application.yml → Spring)
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry}") long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    /**
     * AccessToken 발급.
     * 유효 기간이 짧음 (기본 30분). API 요청마다 Authorization 헤더에 담아서 보냄.
     */
    public String createAccessToken(Long userId) {
        return buildToken(userId, accessTokenExpiry);
    }

    /**
     * RefreshToken 발급.
     * 유효 기간이 길음 (기본 7일). AccessToken이 만료됐을 때 재발급 요청에만 사용.
     * Redis에 저장해서 서버 측에서도 유효성을 관리함.
     */
    public String createRefreshToken(Long userId) {
        return buildToken(userId, refreshTokenExpiry);
    }

    /**
     * 토큰에서 userId 추출.
     * JWT의 subject 필드에 userId를 문자열로 저장했기 때문에 Long으로 파싱해서 반환.
     * validate/validateOrThrow 없이 단독 호출돼도 안전하도록 직접 예외를 감쌈.
     */
    public Long getUserId(String token) {
        try {
            return Long.parseLong(parseClaims(token).getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    /**
     * 토큰 유효성 검사 (예외 없이 boolean 반환).
     * JwtAuthenticationFilter에서 매 요청마다 호출 — 유효하면 true, 만료/변조면 false.
     * 예외를 던지지 않는 이유: 필터에서 예외가 발생하면 Spring의 exceptionHandling이 아닌
     * Servlet 레벨에서 처리되어 GlobalExceptionHandler를 타지 않기 때문.
     */
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Expired JWT token");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 유효성 검사 (실패 시 CustomException 던짐).
     * RefreshToken 재발급 요청(/api/v1/auth/reissue)에서 사용.
     * 여기선 예외를 던져야 GlobalExceptionHandler → 401 응답으로 이어짐.
     */
    public void validateOrThrow(String token) {
        try {
            parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    /**
     * 실제 토큰 문자열 생성 내부 메서드.
     * subject = userId, 발급시각·만료시각 설정 후 HS256으로 서명.
     */
    private String buildToken(Long userId, long expiry) {
        Date now = new Date();
        return Jwts.builder()
                .setIssuer("matchlog-api")
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 서명 검증 + Claims(페이로드) 파싱. 예외를 그대로 올려서 호출자가 목적에 맞게 처리함.
     * - validate: JwtException → false 반환 (필터용)
     * - validateOrThrow: JwtException → CustomException (재발급용)
     * - getUserId: JwtException → CustomException (단독 호출 안전장치)
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer("matchlog-api")
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
