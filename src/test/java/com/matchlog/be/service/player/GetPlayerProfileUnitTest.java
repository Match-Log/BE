package com.matchlog.be.service.player;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.player.PreferredFoot;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.dto.player.response.PlayerProfileResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.PlayerErrorCode;
import com.matchlog.be.repository.PlayerRepository;
import com.matchlog.be.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class GetPlayerProfileUnitTest {

    @Mock private PlayerRepository playerRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private PlayerService playerService;

    @Test
    void 존재하는_선수면_프로필_조회에_성공한다() {
        Long playerId = 1L;
        User user = User.builder().id(1L).email("user@example.com").name("임준혁").build();
        Player player =
                Player.builder()
                        .id(playerId)
                        .user(user)
                        .birthDate(LocalDate.of(1995, 3, 12))
                        .height(178)
                        .weight(72)
                        .preferredFoot(PreferredFoot.RIGHT)
                        .career("전 마포 유나이티드")
                        .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        PlayerProfileResponseDto response = playerService.getPlayerProfile(playerId);

        assertThat(response.getPlayerId()).isEqualTo(playerId);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getHeight()).isEqualTo(178);
        assertThat(response.getPreferredFoot()).isEqualTo(PreferredFoot.RIGHT);
    }

    @Test
    void 존재하지_않는_선수면_PLAYER_NOT_FOUND_예외가_발생한다() {
        Long playerId = 999L;

        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.getPlayerProfile(playerId))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(PlayerErrorCode.PLAYER_NOT_FOUND));
    }
}
