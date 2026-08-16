package com.matchlog.be.service.player;

import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.dto.player.request.RegisterPlayerRequestDto;
import com.matchlog.be.dto.player.response.PlayerProfileResponseDto;
import com.matchlog.be.dto.player.response.RegisterPlayerResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.PlayerErrorCode;
import com.matchlog.be.exception.constant.UserErrorCode;
import com.matchlog.be.repository.PlayerRepository;
import com.matchlog.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    @Transactional
    public RegisterPlayerResponseDto registerPlayer(Long userId, RegisterPlayerRequestDto request) {
        if (playerRepository.existsByUser_Id(userId)) {
            throw new CustomException(PlayerErrorCode.PLAYER_ALREADY_EXISTS);
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Player player =
                Player.create(
                        user,
                        request.getBirthDate(),
                        request.getHeight(),
                        request.getWeight(),
                        request.getPreferredFoot(),
                        request.getCareer());

        return RegisterPlayerResponseDto.from(playerRepository.save(player));
    }

    public PlayerProfileResponseDto getPlayerProfile(Long playerId) {
        Player player =
                playerRepository
                        .findById(playerId)
                        .orElseThrow(() -> new CustomException(PlayerErrorCode.PLAYER_NOT_FOUND));

        return PlayerProfileResponseDto.from(player);
    }
}
