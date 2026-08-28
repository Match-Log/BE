package com.matchlog.be.controller.player;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.constant.player.PreferredFoot;
import com.matchlog.be.dto.player.response.PlayerProfileResponseDto;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Tag("unit")
@WebMvcTest(PlayerController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class GetPlayerProfileControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @MockitoBean private PlayerService playerService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void 존재하는_선수면_200과_프로필을_반환한다() throws Exception {
        PlayerProfileResponseDto response =
                PlayerProfileResponseDto.builder()
                        .playerId(1L)
                        .userId(USER_ID)
                        .birthDate(LocalDate.of(1995, 3, 12))
                        .height(178)
                        .weight(72)
                        .preferredFoot(PreferredFoot.RIGHT)
                        .career("전 마포 유나이티드")
                        .createdAt(LocalDateTime.of(2025, 7, 14, 9, 0))
                        .build();

        when(playerService.getPlayerProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/players/1").with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(1)))
                .andExpect(jsonPath("$.preferredFoot", is("RIGHT")));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/players/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 존재하지_않는_선수면_404_PLAYER_NOT_FOUND를_반환한다() throws Exception {
        when(playerService.getPlayerProfile(999L))
                .thenThrow(new CustomException(PlayerErrorCode.PLAYER_NOT_FOUND));

        mockMvc.perform(get("/api/v1/players/999").with(authentication(authenticatedUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("PLAYER_NOT_FOUND")));
    }
}
