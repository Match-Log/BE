package com.matchlog.be.controller.team;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.matchlog.be.config.SecurityConfig;
import com.matchlog.be.dto.team.request.UpdateTeamRequestDto;
import com.matchlog.be.dto.team.response.UpdateTeamResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.service.team.TeamService;
import com.matchlog.be.util.jwt.JwtTokenProvider;
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
@WebMvcTest(TeamController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class UpdateTeamControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private TeamService teamService;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void MANAGER면_200과_수정된_팀_정보를_반환한다() throws Exception {
        UpdateTeamRequestDto request = UpdateTeamRequestDto.builder().name("한강불사조 FC").build();
        UpdateTeamResponseDto response =
                UpdateTeamResponseDto.builder()
                        .teamId(1L)
                        .name("한강불사조 FC")
                        .updatedAt(LocalDateTime.of(2025, 3, 1, 0, 0))
                        .build();

        when(teamService.updateTeam(eq(USER_ID), eq(1L), any(UpdateTeamRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/teams/1")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId", is(1)))
                .andExpect(jsonPath("$.name", is("한강불사조 FC")));
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        UpdateTeamRequestDto request = UpdateTeamRequestDto.builder().name("한강불사조 FC").build();

        mockMvc.perform(
                        patch("/api/v1/teams/1")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void 존재하지_않는_팀이면_404_TEAM_NOT_FOUND를_반환한다() throws Exception {
        UpdateTeamRequestDto request = UpdateTeamRequestDto.builder().name("한강불사조 FC").build();

        when(teamService.updateTeam(eq(USER_ID), eq(999L), any(UpdateTeamRequestDto.class)))
                .thenThrow(new CustomException(TeamErrorCode.TEAM_NOT_FOUND));

        mockMvc.perform(
                        patch("/api/v1/teams/999")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("TEAM_NOT_FOUND")));
    }

    @Test
    void MANAGER가_아니면_403_FORBIDDEN을_반환한다() throws Exception {
        UpdateTeamRequestDto request = UpdateTeamRequestDto.builder().name("한강불사조 FC").build();

        when(teamService.updateTeam(eq(USER_ID), eq(1L), any(UpdateTeamRequestDto.class)))
                .thenThrow(
                        new CustomException(
                                CommonErrorCode.FORBIDDEN, "팀 정보 수정 권한이 없습니다. (MANAGER만 가능)"));

        mockMvc.perform(
                        patch("/api/v1/teams/1")
                                .with(authentication(authenticatedUser()))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }
}
