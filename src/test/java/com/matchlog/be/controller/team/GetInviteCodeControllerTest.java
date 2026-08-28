package com.matchlog.be.controller.team;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.dto.team.response.InviteCodeResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.service.team.TeamService;
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

@Tag("unit")
@WebMvcTest(TeamController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class GetInviteCodeControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @MockitoBean private TeamService teamService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void MANAGER면_200과_초대코드를_반환한다() throws Exception {
        when(teamService.getInviteCode(USER_ID, 1L))
                .thenReturn(InviteCodeResponseDto.builder().inviteCode("HK4829").build());

        mockMvc.perform(
                        get("/api/v1/teams/1/invite-code")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inviteCode", is("HK4829")));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/teams/1/invite-code"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 존재하지_않는_팀이면_404_TEAM_NOT_FOUND를_반환한다() throws Exception {
        when(teamService.getInviteCode(USER_ID, 999L))
                .thenThrow(new CustomException(TeamErrorCode.TEAM_NOT_FOUND));

        mockMvc.perform(
                        get("/api/v1/teams/999/invite-code")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("TEAM_NOT_FOUND")));
    }

    @Test
    void MANAGER가_아니면_403_FORBIDDEN을_반환한다() throws Exception {
        when(teamService.getInviteCode(USER_ID, 1L))
                .thenThrow(
                        new CustomException(
                                CommonErrorCode.FORBIDDEN, "초대 코드 조회 권한이 없습니다. (MANAGER만 가능)"));

        mockMvc.perform(
                        get("/api/v1/teams/1/invite-code")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }
}
