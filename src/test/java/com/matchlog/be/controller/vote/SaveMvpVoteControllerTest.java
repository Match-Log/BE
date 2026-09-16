package com.matchlog.be.controller.vote;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.dto.vote.request.SubmitMvpVoteRequestDto;
import com.matchlog.be.dto.vote.response.MvpVoteResultItemResponseDto;
import com.matchlog.be.dto.vote.response.MvpVoteStatusResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
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
import tools.jackson.databind.ObjectMapper;

@Tag("unit")
@WebMvcTest(MvpVoteController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class SaveMvpVoteControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private MvpVoteService mvpVoteService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void 유효한_요청이면_200과_투표_현황을_반환한다() throws Exception {
        SubmitMvpVoteRequestDto request =
                SubmitMvpVoteRequestDto.builder().votedPlayerId(9L).build();
        MvpVoteStatusResponseDto response =
                MvpVoteStatusResponseDto.builder()
                        .matchId(1L)
                        .totalVotes(1)
                        .results(
                                List.of(
                                        MvpVoteResultItemResponseDto.builder()
                                                .playerId(9L)
                                                .name("임준혁")
                                                .voteCount(1)
                                                .build()))
                        .winners(List.of(9L))
                        .myVote(9L)
                        .build();

        when(mvpVoteService.saveMvpVote(eq(USER_ID), eq(1L), any(SubmitMvpVoteRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/matches/1/mvp-votes")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId", is(1)))
                .andExpect(jsonPath("$.winners[0]", is(9)))
                .andExpect(jsonPath("$.myVote", is(9)));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        SubmitMvpVoteRequestDto request =
                SubmitMvpVoteRequestDto.builder().votedPlayerId(9L).build();

        mockMvc.perform(
                        put("/api/v1/matches/1/mvp-votes")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void votedPlayerId가_없으면_400_INVALID_REQUEST_BODY를_반환한다() throws Exception {
        SubmitMvpVoteRequestDto request = SubmitMvpVoteRequestDto.builder().build();

        when(mvpVoteService.saveMvpVote(eq(USER_ID), eq(1L), any(SubmitMvpVoteRequestDto.class)))
                .thenThrow(
                        new CustomException(
                                CommonErrorCode.INVALID_REQUEST_BODY, "votedPlayerId는 필수입니다."));

        mockMvc.perform(
                        put("/api/v1/matches/1/mvp-votes")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("INVALID_REQUEST_BODY")));
    }

    @Test
    void 존재하지_않는_경기면_404_MATCH_NOT_FOUND를_반환한다() throws Exception {
        SubmitMvpVoteRequestDto request =
                SubmitMvpVoteRequestDto.builder().votedPlayerId(9L).build();

        when(mvpVoteService.saveMvpVote(eq(USER_ID), eq(999L), any(SubmitMvpVoteRequestDto.class)))
                .thenThrow(new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        mockMvc.perform(
                        put("/api/v1/matches/999/mvp-votes")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("MATCH_NOT_FOUND")));
    }

    @Test
    void 종료되지_않은_경기면_409_MATCH_NOT_FINISHED를_반환한다() throws Exception {
        SubmitMvpVoteRequestDto request =
                SubmitMvpVoteRequestDto.builder().votedPlayerId(9L).build();

        when(mvpVoteService.saveMvpVote(eq(USER_ID), eq(1L), any(SubmitMvpVoteRequestDto.class)))
                .thenThrow(
                        new CustomException(
                                MatchErrorCode.MATCH_NOT_FINISHED, "종료된 경기에만 MVP 투표를 할 수 있습니다."));

        mockMvc.perform(
                        put("/api/v1/matches/1/mvp-votes")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code", is("MATCH_NOT_FINISHED")));
    }
}
