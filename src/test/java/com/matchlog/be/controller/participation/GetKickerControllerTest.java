package com.matchlog.be.controller.participation;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.dto.participation.response.KickerResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.service.participation.ParticipationService;
import com.matchlog.be.util.jwt.JwtTokenProvider;
import java.util.Collections;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Tag("unit")
@WebMvcTest(ParticipationController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class GetKickerControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ParticipationService participationService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void 팀_소속_멤버면_200과_키커_정보를_반환한다() throws Exception {
        KickerResponseDto response =
                KickerResponseDto.builder()
                        .teamId(1L)
                        .playerId(10L)
                        .name("임준혁")
                        .isCaptain(true)
                        .build();

        when(participationService.getKicker(USER_ID, 1L, 10L)).thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/teams/1/players/10/kicker")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(10)))
                .andExpect(jsonPath("$.captain", is(true)));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/teams/1/players/10/kicker"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 팀_소속이_아니면_403_FORBIDDEN을_반환한다() throws Exception {
        when(participationService.getKicker(USER_ID, 1L, 10L))
                .thenThrow(new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다."));

        mockMvc.perform(
                        get("/api/v1/teams/1/players/10/kicker")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }

    @Test
    void 대상_참가정보가_없으면_404_MEMBER_NOT_FOUND를_반환한다() throws Exception {
        when(participationService.getKicker(USER_ID, 1L, 999L))
                .thenThrow(new CustomException(TeamErrorCode.MEMBER_NOT_FOUND));

        mockMvc.perform(
                        get("/api/v1/teams/1/players/999/kicker")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("MEMBER_NOT_FOUND")));
    }
}
