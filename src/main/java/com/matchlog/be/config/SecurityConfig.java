package com.matchlog.be.config;

import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.util.jwt.JwtAuthFilter;
import com.matchlog.be.util.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 전체 설정을 담당하는 클래스.
 *
 * <p>어떤 URL은 누구나 접근 가능하고, 어떤 URL은 로그인해야만 접근 가능한지 설정. JWT 필터를 Spring의 필터 체인에 끼워 넣고, 인증 실패/권한 없음 시 응답
 * 형식도 정의.
 *
 * <p>차이점: - gangku는 JwtAuthenticationEntryPoint, JwtAccessDeniedHandler를 별도 클래스로 분리했지만 여기선 람다로
 * SecurityConfig 안에서 처리함 (파일 수 줄이기 위한 선택). - gangku는 프로필별 H2 콘솔 분기도 포함함. 여기선 단순하게 유지.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    /**
     * 비밀번호 암호화 방식 등록. BCrypt 해시 알고리즘 사용 — 회원가입 시 비밀번호를 DB에 저장하기 전 이걸로 암호화, 로그인 시 입력된 비밀번호와 DB의 해시값을
     * 비교할 때도 사용. AuthService에서 @Autowired로 주입받아 씀.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 보안 규칙 전체 설정 (핵심 메서드).
     *
     * <p>설정 항목: 1. csrf disable: REST API는 세션이 없으니 CSRF 공격 대상이 아님 → 끔. 2. sessionManagement
     * STATELESS: 세션을 전혀 만들지 않음. 매 요청은 JWT로만 인증. 3. authorizeHttpRequests: URL별 접근 권한 설정. -
     * permitAll: 로그인/회원가입 등 인증 없이 접근 허���. - anyRequest().authenticated(): 나머지는 무조건 JWT 필요. 4.
     * exceptionHandling: 인증 실패 시 JSON 에러 응답. 5. addFilterBefore: JwtAuthenticationFilter를
     * UsernamePasswordAuthenticationFilter 앞에 삽입. → Spring의 기본 폼 로그인 필터보다 먼저 실행되어 JWT를 먼저 처리.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/api/v1/auth/signup",
                                                "/api/v1/auth/login",
                                                "/api/v1/auth/reissue",
                                                "/api/v1/auth/check-email",
                                                "/h2-console/**",
                                                "/actuator/health")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        ex ->
                                ex
                                        // 토큰 없이 인증이 필요한 URL에 접근 → 401
                                        .authenticationEntryPoint(
                                                (request, response, authException) ->
                                                        sendErrorResponse(
                                                                response,
                                                                CommonErrorCode.UNAUTHORIZED))
                                        // 토큰은 있지만 권한이 없는 URL에 접근 → 403
                                        .accessDeniedHandler(
                                                (request, response, accessDeniedException) ->
                                                        sendErrorResponse(
                                                                response,
                                                                CommonErrorCode.FORBIDDEN)))
                .addFilterBefore(
                        new JwtAuthFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * FE 오리진 허용 설정. `cors.allowed-origins` 프로퍼티(콤마 구분)로 배포 환경별 오리진을 주입, 기본값은 로컬 개발 서버(CRA/Vite).
     * 쿠키/Authorization 헤더를 주고받아야 하므로 allowCredentials(true) — 이 경우 allowedOrigins에 와일드카드("*")는 쓸 수
     * 없어 allowedOriginPatterns 사용.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Security 예외(401, 403) 발생 시 JSON 형식으로 응답을 직접 작성. Controller를 거치지 않는 필터/핸들러 단에서는 ObjectMapper나
     * ResponseEntity를 쓸 수 없어서 response.getWriter()로 직접 씀. 형식은 GlobalExceptionHandler의
     * ErrorResponse와 동일하게 맞춤.
     */
    private void sendErrorResponse(HttpServletResponse response, CommonErrorCode errorCode)
            throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter()
                .write(
                        String.format(
                                "{\"error\":{\"code\":\"%s\",\"message\":\"%s\"}}",
                                errorCode.getCode(), errorCode.getMessage()));
    }
}
