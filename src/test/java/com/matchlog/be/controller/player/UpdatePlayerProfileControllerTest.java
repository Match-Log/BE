package com.matchlog.be.controller.player;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.constant.player.Career;
import com.matchlog.be.dto.player.request.UpdatePlayerProfileRequestDto;
import com.matchlog.be.dto.player.response.UpdatePlayerProfileResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.PlayerErrorCode;
import com.matchlog.be.service.player.PlayerService;
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
@WebMvcTest(PlayerController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class UpdatePlayerProfileControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private PlayerService playerService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void 본인_프로필이면_200과_수정된_프로필을_반환한다() throws Exception {
        UpdatePlayerProfileRequestDto request =
                UpdatePlayerProfileRequestDto.builder()
                        .career(Career.ACTIVE)
                        .yearsOfExperience(10)
                        .build();
        UpdatePlayerProfileResponseDto response =
                UpdatePlayerProfileResponseDto.builder()
                        .playerId(1L)
                        .userId(USER_ID)
                        .career(Career.ACTIVE)
                        .yearsOfExperience(10)
                        .build();

        when(playerService.updatePlayerProfile(
                        eq(1L), eq(USER_ID), any(UpdatePlayerProfileRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/players/1")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(1)))
                .andExpect(jsonPath("$.career", is("ACTIVE")));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        UpdatePlayerProfileRequestDto request =
                UpdatePlayerProfileRequestDto.builder().career(Career.ACTIVE).build();

        mockMvc.perform(
                        patch("/api/v1/players/1")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 존재하지_않는_선수면_404_PLAYER_NOT_FOUND를_반환한다() throws Exception {
        UpdatePlayerProfileRequestDto request =
                UpdatePlayerProfileRequestDto.builder().career(Career.ACTIVE).build();

        when(playerService.updatePlayerProfile(
                        eq(999L), eq(USER_ID), any(UpdatePlayerProfileRequestDto.class)))
                .thenThrow(new CustomException(PlayerErrorCode.PLAYER_NOT_FOUND));

        mockMvc.perform(
                        patch("/api/v1/players/999")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("PLAYER_NOT_FOUND")));
    }

    @Test
    void 본인_프로필이_아니면_403_FORBIDDEN을_반환한다() throws Exception {
        UpdatePlayerProfileRequestDto request =
                UpdatePlayerProfileRequestDto.builder().career(Career.ACTIVE).build();

        when(playerService.updatePlayerProfile(
                        eq(1L), eq(USER_ID), any(UpdatePlayerProfileRequestDto.class)))
                .thenThrow(new CustomException(CommonErrorCode.FORBIDDEN, "본인 프로필만 수정할 수 있습니다."));

        mockMvc.perform(
                        patch("/api/v1/players/1")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }
}
