package com.matchlog.be.util.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 모든 HTTP 요청에 앞서 JWT를 검사하는 필터.
 *
 * <p>역할 요약: "입구 경비원" Controller에 요청이 도달하기 전, Authorization 헤더를 열어 토큰을 검사하고 유효하면 SecurityContext에 인증
 * 정보를 심어둠. 이후 Controller에서 현재 사용자의 userId를 꺼낼 수 있음.
 *
 * <p>OncePerRequestFilter를 상속: 하나의 요청에 딱 한 번만 실행됨을 보장.
 *
 * <p>DB 조회를 하지 않는 이유: 팀 탈퇴/권한 체크는 Service 레이어에서 Participation 테이블로 처리하고, 회원 탈퇴 후 차단은 AccessToken
 * 만료(30분)에 맡기는 설계 선택. → 매 요청마다 DB 쿼리 1회를 아낌.
 */
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 요청마다 실행되는 핵심 로직.
     *
     * <p>흐름: 1. 헤더에서 "Bearer {token}" 추출 2. 토큰이 유효하면 userId 꺼내기 3. SecurityContext에 인증 정보 등록 (DB 조회
     * 없이 userId만으로 구성) 4. 다음 필터로 넘기기
     *
     * <p>토큰이 없거나 유효하지 않으면 SecurityContext를 건드리지 않고 그냥 통과. → SecurityConfig에서 설정한 접근 제어가 "인증 없음" 상태로
     * 판단하여 401 반환.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validate(token)) {
            Long userId = jwtTokenProvider.getUserId(token);

            // principal 자리에 userId를 직접 넣음.
            // Controller에서 @AuthenticationPrincipal Long userId로 꺼낼 수 있음.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 인증 여부와 상관없이 반드시 다음 필터로 넘겨야 함.
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 토큰 문자열만 추출. "Bearer eyJhbGci..." → "eyJhbGci..." 반환. 헤더가 없거나 형식이 다르면 null
     * 반환.
     */
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearer) && bearer.startsWith(BEARER_PREFIX)) {
            return bearer.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
