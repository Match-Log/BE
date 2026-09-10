package com.matchlog.be.controller.match;

import com.matchlog.be.dto.match.request.CreateMatchRequestDto;
import com.matchlog.be.dto.match.request.UpdateMatchRequestDto;
import com.matchlog.be.dto.match.response.CreateMatchResponseDto;
import com.matchlog.be.dto.match.response.MatchListItemResponseDto;
import com.matchlog.be.dto.match.response.MatchResponseDto;
import com.matchlog.be.dto.match.response.UpdateMatchResponseDto;
import com.matchlog.be.service.match.MatchService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/api/v1/teams/{teamId}/matches")
    public ResponseEntity<CreateMatchResponseDto> createMatch(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long teamId,
            @Valid @RequestBody CreateMatchRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matchService.createMatch(teamId, userId, request));
    }

    @GetMapping("/api/v1/teams/{teamId}/matches")
    public ResponseEntity<List<MatchListItemResponseDto>> getMatches(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long teamId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(matchService.getMatches(teamId, userId, status));
    }

    @GetMapping("/api/v1/matches/{matchId}")
    public ResponseEntity<MatchResponseDto> getMatch(
            @AuthenticationPrincipal Long userId, @PathVariable Long matchId) {
        return ResponseEntity.ok(matchService.getMatch(matchId, userId));
    }

    @PatchMapping("/api/v1/matches/{matchId}")
    public ResponseEntity<UpdateMatchResponseDto> updateMatch(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @RequestBody UpdateMatchRequestDto request) {
        return ResponseEntity.ok(matchService.updateMatch(matchId, userId, request));
    }

    @DeleteMapping("/api/v1/matches/{matchId}")
    public ResponseEntity<Void> deleteMatch(
            @AuthenticationPrincipal Long userId, @PathVariable Long matchId) {
        matchService.deleteMatch(matchId, userId);
        return ResponseEntity.noContent().build();
    }
}
