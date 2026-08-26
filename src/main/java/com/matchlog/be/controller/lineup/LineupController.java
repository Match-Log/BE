package com.matchlog.be.controller.lineup;

import com.matchlog.be.dto.lineup.request.SaveLineupRequestDto;
import com.matchlog.be.dto.lineup.response.LineupResponseDto;
import com.matchlog.be.dto.lineup.response.SaveLineupResponseDto;
import com.matchlog.be.service.lineup.LineupService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/matches/{matchId}/lineup")
@RequiredArgsConstructor
public class LineupController {

    private final LineupService lineupService;

    @GetMapping
    public ResponseEntity<List<LineupResponseDto>> getLineup(
            @AuthenticationPrincipal Long userId, @PathVariable Long matchId) {
        return ResponseEntity.ok(lineupService.getLineup(userId, matchId));
    }

    @PutMapping
    public ResponseEntity<SaveLineupResponseDto> saveLineup(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @Valid @RequestBody SaveLineupRequestDto request) {
        return ResponseEntity.ok(lineupService.saveLineup(userId, matchId, request));
    }
}
