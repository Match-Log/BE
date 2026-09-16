package com.matchlog.be.controller.team;

import com.matchlog.be.dto.team.request.UpdateRoleAssignmentRequestDto;
import com.matchlog.be.dto.team.response.RoleAssignmentResponseDto;
import com.matchlog.be.service.team.TeamRoleAssignmentService;
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
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamRoleAssignmentController {

    private final TeamRoleAssignmentService teamRoleAssignmentService;

    @GetMapping("/{teamId}/role-assignment")
    public ResponseEntity<RoleAssignmentResponseDto> getRoleAssignment(
            @AuthenticationPrincipal Long userId, @PathVariable Long teamId) {
        return ResponseEntity.ok(teamRoleAssignmentService.getRoleAssignment(userId, teamId));
    }

    @PutMapping("/{teamId}/role-assignment")
    public ResponseEntity<RoleAssignmentResponseDto> updateRoleAssignment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long teamId,
            @RequestBody UpdateRoleAssignmentRequestDto request) {
        return ResponseEntity.ok(
                teamRoleAssignmentService.updateRoleAssignment(userId, teamId, request));
    }
}
