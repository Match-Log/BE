package com.matchlog.be.controller.stat;

import com.matchlog.be.dto.stat.request.UpdatePlayerStatRequestDto;
import com.matchlog.be.dto.stat.response.MatchStatListResponseDto;
import com.matchlog.be.dto.stat.response.PlayerStatResponseDto;
import com.matchlog.be.dto.stat.response.UpdatePlayerStatResponseDto;
import com.matchlog.be.service.stat.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matches/{matchId}/stats")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @GetMapping
    public ResponseEntity<MatchStatListResponseDto> getMatchStats(
            @AuthenticationPrincipal Long userId, @PathVariable Long matchId) {
        return ResponseEntity.ok(statService.getMatchStats(userId, matchId));
    }

    @GetMapping("/players/{playerId}")
    public ResponseEntity<PlayerStatResponseDto> getPlayerStat(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @PathVariable Long playerId) {
        return ResponseEntity.ok(statService.getPlayerStat(userId, matchId, playerId));
    }

    @PutMapping("/players/{playerId}")
    public ResponseEntity<UpdatePlayerStatResponseDto> updatePlayerStat(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @PathVariable Long playerId,
            @RequestBody UpdatePlayerStatRequestDto request) {
        return ResponseEntity.ok(statService.updatePlayerStat(userId, matchId, playerId, request));
    }
}
