package com.matchlog.be.controller.stat;

import com.matchlog.be.constant.stat.StatPeriod;
import com.matchlog.be.dto.stat.response.PlayerStatSummaryResponseDto;
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
@RequestMapping("/api/v1/teams/{teamId}/players/{playerId}/stats")
@RequiredArgsConstructor
public class PlayerStatController {

    private final PlayerStatService playerStatService;

    @GetMapping("/summary")
    public ResponseEntity<PlayerStatSummaryResponseDto> getStatSummary(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long teamId,
            @PathVariable Long playerId,
            @RequestParam StatPeriod period,
            @RequestParam int year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(
                playerStatService.getStatSummary(teamId, playerId, userId, period, year, month));
    }
}
