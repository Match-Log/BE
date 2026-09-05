package com.matchlog.be.controller.participation;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.dto.participation.request.UpdateParticipationRequestDto;
import com.matchlog.be.dto.participation.response.UpdateParticipationResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.ParticipationErrorCode;
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
class UpdateParticipationControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ParticipationService participationService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void MANAGER면_200과_수정된_참가정보를_반환한다() throws Exception {
        UpdateParticipationRequestDto request =
                UpdateParticipationRequestDto.builder().number(4).mainPosition("CB").build();
        UpdateParticipationResponseDto response =
                UpdateParticipationResponseDto.builder()
                        .participationId(1L)
                        .teamId(1L)
                        .playerId(10L)
                        .role(ParticipationRole.PLAYER)
                        .number(4)
                        .mainPosition("CB")
                        .updatedAt(LocalDateTime.of(2025, 7, 14, 9, 0))
                        .build();

        when(participationService.updateParticipation(
                        eq(USER_ID), eq(1L), eq(10L), any(UpdateParticipationRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/teams/1/players/10")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number", is(4)))
                .andExpect(jsonPath("$.mainPosition", is("CB")));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        UpdateParticipationRequestDto request =
                UpdateParticipationRequestDto.builder().number(4).build();

        mockMvc.perform(
                        patch("/api/v1/teams/1/players/10")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void MANAGER가_아니면_403_FORBIDDEN을_반환한다() throws Exception {
        UpdateParticipationRequestDto request =
                UpdateParticipationRequestDto.builder().number(4).build();

        when(participationService.updateParticipation(
                        eq(USER_ID), eq(1L), eq(10L), any(UpdateParticipationRequestDto.class)))
                .thenThrow(
                        new CustomException(
                                CommonErrorCode.FORBIDDEN, "참가 정보 수정 권한이 없습니다. (MANAGER만 가능)"));

        mockMvc.perform(
                        patch("/api/v1/teams/1/players/10")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }

    @Test
    void 대상_참가정보가_없으면_404_PARTICIPATION_NOT_FOUND를_반환한다() throws Exception {
        UpdateParticipationRequestDto request =
                UpdateParticipationRequestDto.builder().number(4).build();

        when(participationService.updateParticipation(
                        eq(USER_ID), eq(1L), eq(999L), any(UpdateParticipationRequestDto.class)))
                .thenThrow(new CustomException(ParticipationErrorCode.PARTICIPATION_NOT_FOUND));

        mockMvc.perform(
                        patch("/api/v1/teams/1/players/999")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("PARTICIPATION_NOT_FOUND")));
    }
}
