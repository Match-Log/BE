package com.matchlog.be.controller.participation;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.dto.participation.request.JoinTeamRequestDto;
import com.matchlog.be.dto.participation.response.JoinTeamResponseDto;
import com.matchlog.be.exception.CustomException;
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
class JoinTeamByInviteCodeControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ParticipationService participationService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void 유효한_초대코드면_201과_가입_정보를_반환한다() throws Exception {
        JoinTeamRequestDto request = JoinTeamRequestDto.builder().inviteCode("HK4829").build();
        JoinTeamResponseDto response =
                JoinTeamResponseDto.builder()
                        .participationId(1L)
                        .teamId(1L)
                        .teamName("FC 한강불사조")
                        .playerId(9L)
                        .role(ParticipationRole.PLAYER)
                        .joinedAt(LocalDateTime.of(2025, 3, 1, 0, 0))
                        .build();

        when(participationService.joinTeamByInviteCode(eq(USER_ID), any(JoinTeamRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/teams/join")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.teamId", is(1)))
                .andExpect(jsonPath("$.role", is("PLAYER")));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        JoinTeamRequestDto request = JoinTeamRequestDto.builder().inviteCode("HK4829").build();

        mockMvc.perform(
                        post("/api/v1/teams/join")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 초대코드_형식이_아니면_400_INVALID_REQUEST_BODY를_반환한다() throws Exception {
        JoinTeamRequestDto request = JoinTeamRequestDto.builder().inviteCode("SHORT").build();

        mockMvc.perform(
                        post("/api/v1/teams/join")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("INVALID_REQUEST_BODY")));
    }

    @Test
    void 존재하지_않는_초대코드면_404_INVITE_CODE_NOT_FOUND를_반환한다() throws Exception {
        JoinTeamRequestDto request = JoinTeamRequestDto.builder().inviteCode("ZZZZZZ").build();

        when(participationService.joinTeamByInviteCode(eq(USER_ID), any(JoinTeamRequestDto.class)))
                .thenThrow(new CustomException(TeamErrorCode.INVITE_CODE_NOT_FOUND));

        mockMvc.perform(
                        post("/api/v1/teams/join")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("INVITE_CODE_NOT_FOUND")));
    }

    @Test
    void 이미_가입된_팀이면_409_ALREADY_JOINED를_반환한다() throws Exception {
        JoinTeamRequestDto request = JoinTeamRequestDto.builder().inviteCode("HK4829").build();

        when(participationService.joinTeamByInviteCode(eq(USER_ID), any(JoinTeamRequestDto.class)))
                .thenThrow(new CustomException(TeamErrorCode.ALREADY_JOINED));

        mockMvc.perform(
                        post("/api/v1/teams/join")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code", is("ALREADY_JOINED")));
    }
}
