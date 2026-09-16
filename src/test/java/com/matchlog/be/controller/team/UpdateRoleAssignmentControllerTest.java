package com.matchlog.be.controller.team;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.dto.team.request.UpdateRoleAssignmentRequestDto;
import com.matchlog.be.dto.team.response.RoleAssignmentResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
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
import tools.jackson.databind.ObjectMapper;

@Tag("unit")
@WebMvcTest(TeamRoleAssignmentController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class UpdateRoleAssignmentControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private TeamRoleAssignmentService teamRoleAssignmentService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void MANAGER면_200과_수정된_역할_배정을_반환한다() throws Exception {
        UpdateRoleAssignmentRequestDto request =
                UpdateRoleAssignmentRequestDto.builder().captainParticipationId(9L).build();
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

        when(teamRoleAssignmentService.updateRoleAssignment(
                        eq(USER_ID), eq(1L), any(UpdateRoleAssignmentRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/teams/1/role-assignment")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.captain.participationId", is(9)));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        UpdateRoleAssignmentRequestDto request = UpdateRoleAssignmentRequestDto.builder().build();

        mockMvc.perform(
                        put("/api/v1/teams/1/role-assignment")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void MANAGER가_아니면_403_FORBIDDEN을_반환한다() throws Exception {
        UpdateRoleAssignmentRequestDto request = UpdateRoleAssignmentRequestDto.builder().build();

        when(teamRoleAssignmentService.updateRoleAssignment(
                        eq(USER_ID), eq(1L), any(UpdateRoleAssignmentRequestDto.class)))
                .thenThrow(
                        new CustomException(
                                CommonErrorCode.FORBIDDEN, "세트피스 역할 지정 권한이 없습니다. (MANAGER만 가능)"));

        mockMvc.perform(
                        put("/api/v1/teams/1/role-assignment")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }

    @Test
    void 참가정보가_해당팀_소속이_아니면_404_MEMBER_NOT_FOUND를_반환한다() throws Exception {
        UpdateRoleAssignmentRequestDto request =
                UpdateRoleAssignmentRequestDto.builder().captainParticipationId(20L).build();

        when(teamRoleAssignmentService.updateRoleAssignment(
                        eq(USER_ID), eq(1L), any(UpdateRoleAssignmentRequestDto.class)))
                .thenThrow(new CustomException(TeamErrorCode.MEMBER_NOT_FOUND));

        mockMvc.perform(
                        put("/api/v1/teams/1/role-assignment")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("MEMBER_NOT_FOUND")));
    }
}
