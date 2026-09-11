package com.matchlog.be.service.player;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.lineup.Position;
import com.matchlog.be.constant.player.Career;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.dto.player.request.UpdatePlayerProfileRequestDto;
import com.matchlog.be.dto.player.response.UpdatePlayerProfileResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.PlayerErrorCode;
import com.matchlog.be.repository.PlayerRepository;
import com.matchlog.be.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UpdatePlayerProfileUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private PlayerRepository playerRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private PlayerService playerService;

    @Test
    void 본인_프로필이면_수정에_성공한다() {
        Long playerId = 1L;
        User user = User.builder().id(USER_ID).email("user@example.com").name("임준혁").build();
        Player player = Player.builder().id(playerId).user(user).career(Career.AMATEUR).build();
        UpdatePlayerProfileRequestDto request =
                UpdatePlayerProfileRequestDto.builder()
                        .career(Career.ACTIVE)
                        .yearsOfExperience(10)
                        .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        UpdatePlayerProfileResponseDto response =
                playerService.updatePlayerProfile(playerId, USER_ID, request);

        assertThat(response.getCareer()).isEqualTo(Career.ACTIVE);
        assertThat(response.getYearsOfExperience()).isEqualTo(10);
    }

    @Test
    void 존재하지_않는_선수면_PLAYER_NOT_FOUND_예외가_발생한다() {
        Long playerId = 999L;
        UpdatePlayerProfileRequestDto request = UpdatePlayerProfileRequestDto.builder().build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.updatePlayerProfile(playerId, USER_ID, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(PlayerErrorCode.PLAYER_NOT_FOUND));
    }

    @Test
    void 본인_프로필이_아니면_FORBIDDEN_예외가_발생한다() {
        Long playerId = 1L;
        User otherUser = User.builder().id(2L).email("other@example.com").name("타인").build();
        Player player = Player.builder().id(playerId).user(otherUser).build();
        UpdatePlayerProfileRequestDto request = UpdatePlayerProfileRequestDto.builder().build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        assertThatThrownBy(() -> playerService.updatePlayerProfile(playerId, USER_ID, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }

    @Test
    void 주포지션과_부포지션이_같아지면_DUPLICATE_POSITION_예외가_발생한다() {
        Long playerId = 1L;
        User user = User.builder().id(USER_ID).email("user@example.com").name("임준혁").build();
        Player player =
                Player.builder()
                        .id(playerId)
                        .user(user)
                        .preferredPosition(Position.CM)
                        .subPosition(Position.CB)
                        .build();
        UpdatePlayerProfileRequestDto request =
                UpdatePlayerProfileRequestDto.builder().subPosition(Position.CM).build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        assertThatThrownBy(() -> playerService.updatePlayerProfile(playerId, USER_ID, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(PlayerErrorCode.DUPLICATE_POSITION));
    }
}
