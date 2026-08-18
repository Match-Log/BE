package com.matchlog.be.controller.team;

import com.matchlog.be.dto.team.request.CreateTeamRequestDto;
import com.matchlog.be.dto.team.request.UpdateTeamRequestDto;
import com.matchlog.be.dto.team.response.CreateTeamResponseDto;
import com.matchlog.be.dto.team.response.InviteCodeResponseDto;
import com.matchlog.be.dto.team.response.MyTeamResponseDto;
import com.matchlog.be.dto.team.response.TeamResponseDto;
import com.matchlog.be.dto.team.response.UpdateTeamResponseDto;
import com.matchlog.be.service.team.TeamService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<CreateTeamResponseDto> createTeam(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateTeamRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamService.createTeam(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<MyTeamResponseDto>> getMyTeams(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(teamService.getMyTeams(userId));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponseDto> getTeam(
            @AuthenticationPrincipal Long userId, @PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeam(userId, teamId));
    }

    @GetMapping("/{teamId}/invite-code")
    public ResponseEntity<InviteCodeResponseDto> getInviteCode(
            @AuthenticationPrincipal Long userId, @PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getInviteCode(userId, teamId));
    }

    @PatchMapping("/{teamId}")
    public ResponseEntity<UpdateTeamResponseDto> updateTeam(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long teamId,
            @Valid @RequestBody UpdateTeamRequestDto request) {
        return ResponseEntity.ok(teamService.updateTeam(userId, teamId, request));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(
            @AuthenticationPrincipal Long userId, @PathVariable Long teamId) {
        teamService.deleteTeam(userId, teamId);
        return ResponseEntity.noContent().build();
    }
}
