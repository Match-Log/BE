package com.matchlog.be.controller.team;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.dto.team.request.CreateTeamRequestDto;
import com.matchlog.be.dto.team.response.CreateTeamResponseDto;
import com.matchlog.be.service.team.TeamService;
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
@WebMvcTest(TeamController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class CreateTeamControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private TeamService teamService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void 유효한_요청이면_201과_생성된_팀_정보를_반환한다() throws Exception {
        CreateTeamRequestDto request =
                CreateTeamRequestDto.builder().name("FC 한강불사조").region("서울 마포구").build();

        CreateTeamResponseDto response =
                CreateTeamResponseDto.builder()
                        .teamId(1L)
                        .name("FC 한강불사조")
                        .region("서울 마포구")
                        .inviteCode("HK4829")
                        .createdAt(LocalDateTime.of(2025, 3, 1, 0, 0))
                        .build();

        when(teamService.createTeam(eq(USER_ID), any(CreateTeamRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/teams")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.teamId", is(1)))
                .andExpect(jsonPath("$.inviteCode", is("HK4829")));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        CreateTeamRequestDto request = CreateTeamRequestDto.builder().name("FC 한강불사조").build();

        mockMvc.perform(
                        post("/api/v1/teams")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 이름이_비어있으면_400_INVALID_REQUEST_BODY를_반환한다() throws Exception {
        CreateTeamRequestDto request = CreateTeamRequestDto.builder().name("").build();

        mockMvc.perform(
                        post("/api/v1/teams")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("INVALID_REQUEST_BODY")));
    }
}
