package com.matchlog.be.controller.stat;

import com.matchlog.be.constant.stat.StatPeriod;
import com.matchlog.be.dto.stat.response.TeamPlayerStatsResponseDto;
import com.matchlog.be.service.stat.PlayerStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamPlayerStatController {

    private final PlayerStatService playerStatService;

    @GetMapping("/{teamId}/player-stats")
    public ResponseEntity<TeamPlayerStatsResponseDto> getTeamPlayerStats(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long teamId,
            @RequestParam(required = false) StatPeriod period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(
                playerStatService.getTeamPlayerStats(teamId, userId, period, year, month));
    }
}
