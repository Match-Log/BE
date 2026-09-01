package com.matchlog.be.controller.participation;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.dto.participation.request.AssignKickerRequestDto;
import com.matchlog.be.dto.participation.response.AssignKickerResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.service.participation.ParticipationService;
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
@WebMvcTest(ParticipationController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class AssignKickerControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ParticipationService participationService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void MANAGER면_200과_지정된_키커_정보를_반환한다() throws Exception {
        AssignKickerRequestDto request = AssignKickerRequestDto.builder().isCaptain(true).build();
        AssignKickerResponseDto response =
                AssignKickerResponseDto.builder()
                        .teamId(1L)
                        .playerId(10L)
                        .isCaptain(true)
                        .updatedAt(LocalDateTime.of(2025, 3, 1, 0, 0))
                        .build();

        when(participationService.assignKicker(
                        eq(USER_ID), eq(1L), eq(10L), any(AssignKickerRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/teams/1/players/10/kicker")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(10)))
                .andExpect(jsonPath("$.captain", is(true)));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        AssignKickerRequestDto request = AssignKickerRequestDto.builder().isCaptain(true).build();

        mockMvc.perform(
                        put("/api/v1/teams/1/players/10/kicker")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void MANAGER가_아니면_403_FORBIDDEN을_반환한다() throws Exception {
        AssignKickerRequestDto request = AssignKickerRequestDto.builder().isCaptain(true).build();

        when(participationService.assignKicker(
                        eq(USER_ID), eq(1L), eq(10L), any(AssignKickerRequestDto.class)))
                .thenThrow(
                        new CustomException(
                                CommonErrorCode.FORBIDDEN, "전담 키커 지정 권한이 없습니다. (MANAGER만 가능)"));

        mockMvc.perform(
                        put("/api/v1/teams/1/players/10/kicker")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }

    @Test
    void 대상_참가정보가_없으면_404_MEMBER_NOT_FOUND를_반환한다() throws Exception {
        AssignKickerRequestDto request = AssignKickerRequestDto.builder().isCaptain(true).build();

        when(participationService.assignKicker(
                        eq(USER_ID), eq(1L), eq(999L), any(AssignKickerRequestDto.class)))
                .thenThrow(new CustomException(TeamErrorCode.MEMBER_NOT_FOUND));

        mockMvc.perform(
                        put("/api/v1/teams/1/players/999/kicker")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("MEMBER_NOT_FOUND")));
    }

    @Test
    void 이미_다른_선수가_주장이면_409_CAPTAIN_ALREADY_ASSIGNED를_반환한다() throws Exception {
        AssignKickerRequestDto request = AssignKickerRequestDto.builder().isCaptain(true).build();

        when(participationService.assignKicker(
                        eq(USER_ID), eq(1L), eq(10L), any(AssignKickerRequestDto.class)))
                .thenThrow(new CustomException(TeamErrorCode.CAPTAIN_ALREADY_ASSIGNED));

        mockMvc.perform(
                        put("/api/v1/teams/1/players/10/kicker")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code", is("CAPTAIN_ALREADY_ASSIGNED")));
    }
}
