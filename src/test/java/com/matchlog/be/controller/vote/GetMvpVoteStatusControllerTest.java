package com.matchlog.be.controller.vote;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.dto.vote.response.MvpVoteStatusResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.service.vote.MvpVoteService;
import com.matchlog.be.util.jwt.JwtTokenProvider;
import java.util.Collections;
import java.util.List;
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
@WebMvcTest(MvpVoteController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class GetMvpVoteStatusControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @MockitoBean private MvpVoteService mvpVoteService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void 팀_소속_멤버면_200과_투표_현황을_반환한다() throws Exception {
        MvpVoteStatusResponseDto response =
                MvpVoteStatusResponseDto.builder()
                        .matchId(1L)
                        .totalVotes(0)
                        .results(List.of())
                        .winners(List.of())
                        .myVote(null)
                        .build();

        when(mvpVoteService.getMvpVoteStatus(USER_ID, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/matches/1/mvp-votes").with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId", is(1)))
                .andExpect(jsonPath("$.totalVotes", is(0)));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/matches/1/mvp-votes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 존재하지_않는_경기면_404_MATCH_NOT_FOUND를_반환한다() throws Exception {
        when(mvpVoteService.getMvpVoteStatus(USER_ID, 999L))
                .thenThrow(new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        mockMvc.perform(
                        get("/api/v1/matches/999/mvp-votes").with(authentication(authenticatedUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("MATCH_NOT_FOUND")));
    }
}