package com.matchlog.be.service.player;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.player.PreferredFoot;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.dto.player.request.RegisterPlayerRequestDto;
import com.matchlog.be.dto.player.response.RegisterPlayerResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.PlayerErrorCode;
import com.matchlog.be.exception.constant.UserErrorCode;
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
class RegisterPlayerUnitTest {

    @Mock private PlayerRepository playerRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private PlayerService playerService;

    @Test
    void 아직_선수_등록을_하지_않은_유저면_선수_등록에_성공한다() {
        Long userId = 1L;
        User user = User.builder().id(userId).email("user@example.com").name("임준혁").build();
        RegisterPlayerRequestDto request =
                RegisterPlayerRequestDto.builder()
                        .birthDate(LocalDate.of(1995, 3, 12))
                        .height(178)
                        .weight(72)
                        .preferredFoot(PreferredFoot.RIGHT)
                        .career("전 마포 유나이티드")
                        .build();

        when(playerRepository.existsByUser_Id(userId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(playerRepository.save(any(Player.class)))
                .thenAnswer(
                        invocation -> {
                            Player saved = invocation.getArgument(0);
                            return Player.builder()
                                    .id(1L)
                                    .user(saved.getUser())
                                    .birthDate(saved.getBirthDate())
                                    .height(saved.getHeight())
                                    .weight(saved.getWeight())
                                    .preferredFoot(saved.getPreferredFoot())
                                    .career(saved.getCareer())
                                    .build();
                        });

        RegisterPlayerResponseDto response = playerService.registerPlayer(userId, request);

        assertThat(response.getPlayerId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getHeight()).isEqualTo(178);
        assertThat(response.getPreferredFoot()).isEqualTo(PreferredFoot.RIGHT);
    }

    @Test
    void 이미_선수로_등록된_유저면_PLAYER_ALREADY_EXISTS_예외가_발생한다() {
        Long userId = 1L;
        RegisterPlayerRequestDto request = RegisterPlayerRequestDto.builder().build();

        when(playerRepository.existsByUser_Id(userId)).thenReturn(true);

        assertThatThrownBy(() -> playerService.registerPlayer(userId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(PlayerErrorCode.PLAYER_ALREADY_EXISTS));
    }

    @Test
    void 토큰의_userId에_해당하는_유저가_존재하지_않으면_USER_NOT_FOUND_예외가_발생한다() {
        Long userId = 1L;
        RegisterPlayerRequestDto request = RegisterPlayerRequestDto.builder().build();

        when(playerRepository.existsByUser_Id(userId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.registerPlayer(userId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }
}
