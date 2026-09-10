package com.matchlog.be.controller.match;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.dto.match.response.MatchResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.service.match.MatchService;
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
@WebMvcTest(MatchController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class GetMatchControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @MockitoBean private MatchService matchService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void 팀_소속이면_200과_경기_상세를_반환한다() throws Exception {
        MatchResponseDto response =
                MatchResponseDto.builder().matchId(1L).teamId(1L).opponent("서울 드래곤즈").build();

        when(matchService.getMatch(1L, USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/matches/1").with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId", is(1)))
                .andExpect(jsonPath("$.opponent", is("서울 드래곤즈")));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/matches/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 존재하지_않는_경기면_404_MATCH_NOT_FOUND를_반환한다() throws Exception {
        when(matchService.getMatch(999L, USER_ID))
                .thenThrow(new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        mockMvc.perform(get("/api/v1/matches/999").with(authentication(authenticatedUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("MATCH_NOT_FOUND")));
    }

    @Test
    void 경기의_팀_소속이_아니면_403_FORBIDDEN을_반환한다() throws Exception {
        when(matchService.getMatch(1L, USER_ID))
                .thenThrow(new CustomException(CommonErrorCode.FORBIDDEN, "해당 경기에 접근 권한이 없습니다."));

        mockMvc.perform(get("/api/v1/matches/1").with(authentication(authenticatedUser())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }
}
