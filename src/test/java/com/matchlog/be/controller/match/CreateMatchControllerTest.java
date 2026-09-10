package com.matchlog.be.controller.match;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.constant.match.HomeAway;
import com.matchlog.be.constant.match.MatchType;
import com.matchlog.be.dto.match.request.CreateMatchRequestDto;
import com.matchlog.be.dto.match.response.CreateMatchResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.service.match.MatchService;
import com.matchlog.be.util.jwt.JwtTokenProvider;
import java.time.LocalDateTime;
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
import tools.jackson.databind.ObjectMapper;

@Tag("unit")
@WebMvcTest(MatchController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class CreateMatchControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private MatchService matchService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    private CreateMatchRequestDto validRequest() {
        return CreateMatchRequestDto.builder()
                .opponent("서울 드래곤즈")
                .matchDate(LocalDateTime.of(2099, 7, 14, 7, 0))
                .location("한강공원 풋살장")
                .homeAway(HomeAway.HOME)
                .matchType(MatchType.SOCCER)
                .build();
    }

    @Test
    void 유효한_요청이면_201과_생성된_경기_정보를_반환한다() throws Exception {
        CreateMatchResponseDto response =
                CreateMatchResponseDto.builder()
                        .matchId(1L)
                        .teamId(1L)
                        .opponent("서울 드래곤즈")
                        .boardId(5L)
                        .boardTitle("7/14 vs 서울 드래곤즈 참석 투표")
                        .build();

        when(matchService.createMatch(eq(1L), eq(USER_ID), any(CreateMatchRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/teams/1/matches")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matchId", is(1)))
                .andExpect(jsonPath("$.boardId", is(5)))
                .andExpect(jsonPath("$.boardTitle", is("7/14 vs 서울 드래곤즈 참석 투표")));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        mockMvc.perform(
                        post("/api/v1/teams/1/matches")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 상대팀이_비어있으면_400_INVALID_REQUEST_BODY를_반환한다() throws Exception {
        CreateMatchRequestDto request =
                CreateMatchRequestDto.builder()
                        .opponent("")
                        .matchDate(LocalDateTime.of(2099, 7, 14, 7, 0))
                        .homeAway(HomeAway.HOME)
                        .matchType(MatchType.SOCCER)
                        .build();

        mockMvc.perform(
                        post("/api/v1/teams/1/matches")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("INVALID_REQUEST_BODY")));
    }

    @Test
    void 존재하지_않는_팀이면_404_TEAM_NOT_FOUND를_반환한다() throws Exception {
        when(matchService.createMatch(eq(999L), eq(USER_ID), any(CreateMatchRequestDto.class)))
                .thenThrow(new CustomException(TeamErrorCode.TEAM_NOT_FOUND));

        mockMvc.perform(
                        post("/api/v1/teams/999/matches")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("TEAM_NOT_FOUND")));
    }

    @Test
    void MANAGER가_아니면_403_FORBIDDEN을_반환한다() throws Exception {
        when(matchService.createMatch(eq(1L), eq(USER_ID), any(CreateMatchRequestDto.class)))
                .thenThrow(
                        new CustomException(
                                CommonErrorCode.FORBIDDEN, "경기 생성 권한이 없습니다. (MANAGER만 가능)"));

        mockMvc.perform(
                        post("/api/v1/teams/1/matches")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }

    @Test
    void 동일_날짜에_이미_경기가_있으면_409_MATCH_ALREADY_EXISTS를_반환한다() throws Exception {
        when(matchService.createMatch(eq(1L), eq(USER_ID), any(CreateMatchRequestDto.class)))
                .thenThrow(new CustomException(MatchErrorCode.MATCH_ALREADY_EXISTS));

        mockMvc.perform(
                        post("/api/v1/teams/1/matches")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code", is("MATCH_ALREADY_EXISTS")));
    }
}
