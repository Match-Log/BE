package com.matchlog.be.controller.team;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.dto.team.response.MyTeamResponseDto;
import com.matchlog.be.service.team.TeamService;
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

@Tag("unit")
@WebMvcTest(TeamController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class GetMyTeamsControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @MockitoBean private TeamService teamService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void 인증된_유저면_200과_소속_팀_목록을_반환한다() throws Exception {
        List<MyTeamResponseDto> response =
                List.of(
                        MyTeamResponseDto.builder()
                                .teamId(1L)
                                .name("FC 한강불사조")
                                .role("PLAYER")
                                .build(),
                        MyTeamResponseDto.builder()
                                .teamId(2L)
                                .name("마포 유나이티드")
                                .role("MANAGER")
                                .build());

        when(teamService.getMyTeams(USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/teams").with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].teamId", is(1)))
                .andExpect(jsonPath("$[1].role", is("MANAGER")));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/teams"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }
}
