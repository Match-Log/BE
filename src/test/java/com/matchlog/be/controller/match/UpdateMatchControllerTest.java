package com.matchlog.be.controller.match;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.dto.match.request.UpdateMatchRequestDto;
import com.matchlog.be.dto.match.response.UpdateMatchResponseDto;
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
import tools.jackson.databind.ObjectMapper;

@Tag("unit")
@WebMvcTest(MatchController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class UpdateMatchControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private MatchService matchService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void MANAGER면_200과_수정된_경기_정보를_반환한다() throws Exception {
        UpdateMatchRequestDto request =
                UpdateMatchRequestDto.builder().scoreHome(3).scoreAway(1).isFinished(true).build();
        UpdateMatchResponseDto response =
                UpdateMatchResponseDto.builder()
                        .matchId(1L)
                        .opponent("서울 드래곤즈")
                        .isFinished(true)
                        .scoreHome(3)
                        .scoreAway(1)
                        .build();

        when(matchService.updateMatch(eq(1L), eq(USER_ID), any(UpdateMatchRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/matches/1")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId", is(1)))
                .andExpect(jsonPath("$.finished", is(true)));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        UpdateMatchRequestDto request = UpdateMatchRequestDto.builder().opponent("변경팀").build();

        mockMvc.perform(
                        patch("/api/v1/matches/1")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 존재하지_않는_경기면_404_MATCH_NOT_FOUND를_반환한다() throws Exception {
        UpdateMatchRequestDto request = UpdateMatchRequestDto.builder().opponent("변경팀").build();

        when(matchService.updateMatch(eq(999L), eq(USER_ID), any(UpdateMatchRequestDto.class)))
                .thenThrow(new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        mockMvc.perform(
                        patch("/api/v1/matches/999")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("MATCH_NOT_FOUND")));
    }

    @Test
    void MANAGER가_아니면_403_FORBIDDEN을_반환한다() throws Exception {
        UpdateMatchRequestDto request = UpdateMatchRequestDto.builder().opponent("변경팀").build();

        when(matchService.updateMatch(eq(1L), eq(USER_ID), any(UpdateMatchRequestDto.class)))
                .thenThrow(
                        new CustomException(
                                CommonErrorCode.FORBIDDEN, "경기 수정 권한이 없습니다. (MANAGER만 가능)"));

        mockMvc.perform(
                        patch("/api/v1/matches/1")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }
}
