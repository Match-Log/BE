package com.matchlog.be.controller.player;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.constant.player.PreferredFoot;
import com.matchlog.be.dto.player.request.RegisterPlayerRequestDto;
import com.matchlog.be.dto.player.response.RegisterPlayerResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.PlayerErrorCode;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.util.jwt.JwtTokenProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@Tag("unit")
@WebMvcTest(PlayerController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class RegisterPlayerControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private PlayerService playerService;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void 인증된_유저의_요청이면_201과_등록된_선수_정보를_반환한다() throws Exception {
        RegisterPlayerRequestDto request =
                RegisterPlayerRequestDto.builder()
                        .birthDate(LocalDate.of(1995, 3, 12))
                        .height(178)
                        .weight(72)
                        .preferredFoot(PreferredFoot.RIGHT)
                        .career("전 마포 유나이티드")
                        .build();

        RegisterPlayerResponseDto response =
                RegisterPlayerResponseDto.builder()
                        .playerId(1L)
                        .userId(USER_ID)
                        .birthDate(LocalDate.of(1995, 3, 12))
                        .height(178)
                        .weight(72)
                        .preferredFoot(PreferredFoot.RIGHT)
                        .career("전 마포 유나이티드")
                        .createdAt(LocalDateTime.of(2025, 7, 14, 9, 0))
                        .build();

        when(playerService.registerPlayer(eq(USER_ID), any(RegisterPlayerRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/players")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playerId", is(1)))
                .andExpect(jsonPath("$.userId", is(1)));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        RegisterPlayerRequestDto request = RegisterPlayerRequestDto.builder().build();

        mockMvc.perform(
                        post("/api/v1/players")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 이미_선수_등록된_유저면_409_PLAYER_ALREADY_EXISTS를_반환한다() throws Exception {
        RegisterPlayerRequestDto request = RegisterPlayerRequestDto.builder().build();

        when(playerService.registerPlayer(eq(USER_ID), any(RegisterPlayerRequestDto.class)))
                .thenThrow(new CustomException(PlayerErrorCode.PLAYER_ALREADY_EXISTS));

        mockMvc.perform(
                        post("/api/v1/players")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code", is("PLAYER_ALREADY_EXISTS")));
    }
}
