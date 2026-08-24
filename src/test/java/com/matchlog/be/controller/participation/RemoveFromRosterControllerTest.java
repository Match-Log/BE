package com.matchlog.be.controller.participation;

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
import com.matchlog.be.exception.constant.ParticipationErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.service.participation.ParticipationService;
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
@WebMvcTest(ParticipationController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class RemoveFromRosterControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ParticipationService participationService;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, Collections.emptyList());
    }

    @Test
    void MANAGER면_204를_반환한다() throws Exception {
        doNothing().when(participationService).removeFromRoster(USER_ID, 1L, 10L);

        mockMvc.perform(
                        delete("/api/v1/teams/1/players/10")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isNoContent());
    }

    @Test
    void 토큰_없이_요청하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/1/players/10"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    void MANAGER가_아니면_403_FORBIDDEN을_반환한다() throws Exception {
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "팀원 제외 권한이 없습니다. (MANAGER만 가능)"))
                .when(participationService)
                .removeFromRoster(USER_ID, 1L, 10L);

        mockMvc.perform(
                        delete("/api/v1/teams/1/players/10")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }

    @Test
    void 대상_참가정보가_없으면_404_PARTICIPATION_NOT_FOUND를_반환한다() throws Exception {
        doThrow(new CustomException(ParticipationErrorCode.PARTICIPATION_NOT_FOUND))
                .when(participationService)
                .removeFromRoster(USER_ID, 1L, 999L);

        mockMvc.perform(
                        delete("/api/v1/teams/1/players/999")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("PARTICIPATION_NOT_FOUND")));
    }

    @Test
    void 마지막_MANAGER면_409_LAST_MANAGER_CANNOT_BE_REMOVED를_반환한다() throws Exception {
        doThrow(new CustomException(TeamErrorCode.LAST_MANAGER_CANNOT_BE_REMOVED))
                .when(participationService)
                .removeFromRoster(USER_ID, 1L, 9L);

        mockMvc.perform(
                        delete("/api/v1/teams/1/players/9")
                                .with(authentication(authenticatedUser())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code", is("LAST_MANAGER_CANNOT_BE_REMOVED")));
    }
}
