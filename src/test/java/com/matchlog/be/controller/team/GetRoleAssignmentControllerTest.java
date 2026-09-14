package com.matchlog.be.controller.team;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.dto.team.response.RoleAssignmentResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.service.team.TeamRoleAssignmentService;
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
@WebMvcTest(TeamRoleAssignmentController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class GetRoleAssignmentControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @MockitoBean private TeamRoleAssignmentService teamRoleAssignmentService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void 팀원이면_200과_역할_배정을_반환한다() throws Exception {
        RoleAssignmentResponseDto response =
                RoleAssignmentResponseDto.builder()
                        .teamId(1L)
                        .captain(
                                RoleAssignmentResponseDto.RoleSlot.builder()
                                        .participationId(9L)
                                        .playerId(9L)
                                        .name("임준혁")
                                        .build())
                        .build();

        when(teamRoleAssignmentService.getRoleAssignment(USER_ID, 1L)).thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/teams/1/role-assignment")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId", is(1)))
                .andExpect(jsonPath("$.captain.name", is("임준혁")))
                .andExpect(jsonPath("$.pkTaker", is(nullValue())));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/teams/1/role-assignment"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 팀_소속이_아니면_403_FORBIDDEN을_반환한다() throws Exception {
        when(teamRoleAssignmentService.getRoleAssignment(USER_ID, 1L))
                .thenThrow(new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다."));

        mockMvc.perform(
                        get("/api/v1/teams/1/role-assignment")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }
}
