package com.matchlog.be.controller.team;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Tag("unit")
@WebMvcTest(TeamController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class DeleteTeamControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @MockitoBean private TeamService teamService;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void MANAGER면_204를_반환한다() throws Exception {
        doNothing().when(teamService).deleteTeam(USER_ID, 1L);

        mockMvc.perform(delete("/api/v1/teams/1").with(authentication(authenticatedUser())))
                .andExpect(status().isNoContent());
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 존재하지_않는_팀이면_404_TEAM_NOT_FOUND를_반환한다() throws Exception {
        doThrow(new CustomException(TeamErrorCode.TEAM_NOT_FOUND))
                .when(teamService)
                .deleteTeam(USER_ID, 999L);

        mockMvc.perform(delete("/api/v1/teams/999").with(authentication(authenticatedUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("TEAM_NOT_FOUND")));
    }

    @Test
    void MANAGER가_아니면_403_FORBIDDEN을_반환한다() throws Exception {
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "팀 삭제 권한이 없습니다. (MANAGER만 가능)"))
                .when(teamService)
                .deleteTeam(USER_ID, 1L);

        mockMvc.perform(delete("/api/v1/teams/1").with(authentication(authenticatedUser())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }
}
