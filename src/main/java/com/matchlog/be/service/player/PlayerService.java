package com.matchlog.be.service.player;

import com.matchlog.be.constant.lineup.Position;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.dto.player.request.RegisterPlayerRequestDto;
import com.matchlog.be.dto.player.request.UpdatePlayerProfileRequestDto;
import com.matchlog.be.dto.player.response.PlayerProfileResponseDto;
import com.matchlog.be.dto.player.response.RegisterPlayerResponseDto;
import com.matchlog.be.dto.player.response.UpdatePlayerProfileResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
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

        validatePositionsDiffer(request.getPreferredPosition(), request.getSubPosition());

        Player player =
                Player.create(
                        user,
                        request.getBirthDate(),
                        request.getHeight(),
                        request.getWeight(),
                        request.getPreferredFoot(),
                        request.getCareer(),
                        request.getYearsOfExperience(),
                        request.getPreferredPosition(),
                        request.getSubPosition());

        return RegisterPlayerResponseDto.from(playerRepository.save(player));
    }

    @Transactional
    public UpdatePlayerProfileResponseDto updatePlayerProfile(
            Long playerId, Long userId, UpdatePlayerProfileRequestDto request) {
        Player player =
                playerRepository
                        .findById(playerId)
                        .orElseThrow(() -> new CustomException(PlayerErrorCode.PLAYER_NOT_FOUND));

        if (!player.getUser().getId().equals(userId)) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "본인 프로필만 수정할 수 있습니다.");
        }

        player.updateProfile(
                request.getBirthDate(),
                request.getHeight(),
                request.getWeight(),
                request.getPreferredFoot(),
                request.getCareer(),
                request.getYearsOfExperience(),
                request.getPreferredPosition(),
                request.getSubPosition());

        validatePositionsDiffer(player.getPreferredPosition(), player.getSubPosition());

        return UpdatePlayerProfileResponseDto.from(player);
    }

    private void validatePositionsDiffer(Position preferredPosition, Position subPosition) {
        if (preferredPosition != null && preferredPosition.equals(subPosition)) {
            throw new CustomException(PlayerErrorCode.DUPLICATE_POSITION);
        }
    }

    public PlayerProfileResponseDto getPlayerProfile(Long playerId) {
        Player player =
                playerRepository
                        .findById(playerId)
                        .orElseThrow(() -> new CustomException(PlayerErrorCode.PLAYER_NOT_FOUND));

        return PlayerProfileResponseDto.from(player);
    }

    /**
     * JWT의 userId를 현재 요청자의 Player 엔티티로 변환. 팀/경기/투표 등 대부분의 도메인에서 "현재 선수"를 특정할 때 재사용. 선수 등록을 하지 않은
     * 유저가 접근하면 PLAYER_NOT_FOUND.
     */
    public Player getCurrentPlayer(Long userId) {
        return playerRepository
                .findByUser_Id(userId)
                .orElseThrow(() -> new CustomException(PlayerErrorCode.PLAYER_NOT_FOUND));
    }
}
