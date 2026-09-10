package com.matchlog.be.controller.tactic;

import com.matchlog.be.dto.tactic.request.SaveTacticRequestDto;
import com.matchlog.be.dto.tactic.response.TacticResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.TacticErrorCode;
import com.matchlog.be.service.tactic.TacticService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matches/{matchId}/tactics")
@RequiredArgsConstructor
public class TacticController {

    private final TacticService tacticService;

    @GetMapping
    public ResponseEntity<?> getTactics(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @RequestParam(required = false) String scope) {
        if ("team".equals(scope)) {
            return ResponseEntity.ok(tacticService.getTeamTactic(userId, matchId));
        }
        throw new CustomException(TacticErrorCode.INVALID_SCOPE);
    }

    @PutMapping
    public ResponseEntity<TacticResponseDto> saveTeamTactic(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @RequestParam(required = false) String scope,
            @RequestBody SaveTacticRequestDto request) {
        if (!"team".equals(scope)) {
            throw new CustomException(TacticErrorCode.INVALID_SCOPE);
        }
        return ResponseEntity.ok(tacticService.saveTeamTactic(userId, matchId, request));
    }

    @GetMapping("/players")
    public ResponseEntity<List<TacticResponseDto>> getAllPersonalTactics(
            @AuthenticationPrincipal Long userId, @PathVariable Long matchId) {
        return ResponseEntity.ok(tacticService.getAllPersonalTactics(userId, matchId));
    }

    @GetMapping("/players/{playerId}")
    public ResponseEntity<TacticResponseDto> getPersonalTactic(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @PathVariable Long playerId) {
        return ResponseEntity.ok(tacticService.getPersonalTactic(userId, matchId, playerId));
    }

    @PutMapping("/players/{playerId}")
    public ResponseEntity<TacticResponseDto> savePersonalTactic(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @PathVariable Long playerId,
            @RequestBody SaveTacticRequestDto request) {
        return ResponseEntity.ok(
                tacticService.savePersonalTactic(userId, matchId, playerId, request));
    }
}
