package com.matchlog.be.controller.stat;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.constant.stat.StatPeriod;
import com.matchlog.be.dto.stat.response.PlayerStatSummaryResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.service.stat.PlayerStatService;
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
@WebMvcTest(PlayerStatController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class GetPlayerStatSummaryControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @MockitoBean private PlayerStatService playerStatService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void 유효한_요청이면_200과_집계_결과를_반환한다() throws Exception {
        PlayerStatSummaryResponseDto response =
                PlayerStatSummaryResponseDto.builder()
                        .playerId(9L)
                        .period(StatPeriod.MONTHLY)
                        .year(2026)
                        .month(9)
                        .matchCount(4)
                        .goals(2)
                        .assists(1)
                        .mvpCount(1)
                        .averageRating(7.5)
                        .build();

        when(playerStatService.getStatSummary(1L, 9L, USER_ID, StatPeriod.MONTHLY, 2026, 9))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/teams/1/players/9/stats/summary")
                                .param("period", "MONTHLY")
                                .param("year", "2026")
                                .param("month", "9")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(9)))
                .andExpect(jsonPath("$.matchCount", is(4)))
                .andExpect(jsonPath("$.averageRating", is(7.5)));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        mockMvc.perform(
                        get("/api/v1/teams/1/players/9/stats/summary")
                                .param("period", "SEASON")
                                .param("year", "2026"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void year_파라미터가_없으면_400_INVALID_REQUEST_BODY를_반환한다() throws Exception {
        mockMvc.perform(
                        get("/api/v1/teams/1/players/9/stats/summary")
                                .param("period", "SEASON")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("INVALID_REQUEST_BODY")));
    }

    @Test
    void period_파라미터가_없으면_400_INVALID_REQUEST_BODY를_반환한다() throws Exception {
        mockMvc.perform(
                        get("/api/v1/teams/1/players/9/stats/summary")
                                .param("year", "2026")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("INVALID_REQUEST_BODY")));
    }

    @Test
    void MONTHLY인데_month가_없으면_400_INVALID_REQUEST_BODY를_반환한다() throws Exception {
        when(playerStatService.getStatSummary(1L, 9L, USER_ID, StatPeriod.MONTHLY, 2026, null))
                .thenThrow(
                        new CustomException(
                                CommonErrorCode.INVALID_REQUEST_BODY,
                                "period=MONTHLY일 때 month는 1~12 사이여야 합니다."));

        mockMvc.perform(
                        get("/api/v1/teams/1/players/9/stats/summary")
                                .param("period", "MONTHLY")
                                .param("year", "2026")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("INVALID_REQUEST_BODY")));
    }

    @Test
    void 대상_선수가_팀_소속이_아니면_404_MEMBER_NOT_FOUND를_반환한다() throws Exception {
        when(playerStatService.getStatSummary(1L, 999L, USER_ID, StatPeriod.SEASON, 2026, null))
                .thenThrow(new CustomException(TeamErrorCode.MEMBER_NOT_FOUND));

        mockMvc.perform(
                        get("/api/v1/teams/1/players/999/stats/summary")
                                .param("period", "SEASON")
                                .param("year", "2026")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("MEMBER_NOT_FOUND")));
    }
}
