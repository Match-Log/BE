package com.matchlog.be.controller.participation;

import com.matchlog.be.dto.participation.request.AssignKickerRequestDto;
import com.matchlog.be.dto.participation.request.JoinTeamRequestDto;
import com.matchlog.be.dto.participation.request.UpdateParticipationRequestDto;
import com.matchlog.be.dto.participation.response.AssignKickerResponseDto;
import com.matchlog.be.dto.participation.response.JoinTeamResponseDto;
import com.matchlog.be.dto.participation.response.KickerResponseDto;
import com.matchlog.be.dto.participation.response.RosterItemResponseDto;
import com.matchlog.be.dto.participation.response.UpdateParticipationResponseDto;
import com.matchlog.be.service.participation.ParticipationService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipationService participationService;

    @PostMapping("/join")
    public ResponseEntity<JoinTeamResponseDto> joinTeamByInviteCode(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody JoinTeamRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(participationService.joinTeamByInviteCode(userId, request));
    }

    @GetMapping("/{teamId}/players")
    public ResponseEntity<List<RosterItemResponseDto>> getRoster(
            @AuthenticationPrincipal Long userId, @PathVariable Long teamId) {
        return ResponseEntity.ok(participationService.getRoster(userId, teamId));
    }

    @DeleteMapping("/{teamId}/players/{playerId}")
    public ResponseEntity<Void> removeFromRoster(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long teamId,
            @PathVariable Long playerId) {
        participationService.removeFromRoster(userId, teamId, playerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{teamId}/players/{playerId}/kicker")
    public ResponseEntity<AssignKickerResponseDto> assignKicker(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long teamId,
            @PathVariable Long playerId,
            @RequestBody AssignKickerRequestDto request) {
        return ResponseEntity.ok(
                participationService.assignKicker(userId, teamId, playerId, request));
    }

    @GetMapping("/{teamId}/players/{playerId}/kicker")
    public ResponseEntity<KickerResponseDto> getKicker(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long teamId,
            @PathVariable Long playerId) {
        return ResponseEntity.ok(participationService.getKicker(userId, teamId, playerId));
    }

    @PatchMapping("/{teamId}/players/{playerId}")
    public ResponseEntity<UpdateParticipationResponseDto> updateParticipation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long teamId,
            @PathVariable Long playerId,
            @RequestBody UpdateParticipationRequestDto request) {
        return ResponseEntity.ok(
                participationService.updateParticipation(userId, teamId, playerId, request));
    }
}
