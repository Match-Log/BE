package com.matchlog.be.controller.participation;

import com.matchlog.be.dto.participation.request.JoinTeamRequestDto;
import com.matchlog.be.dto.participation.response.JoinTeamResponseDto;
import com.matchlog.be.service.participation.ParticipationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
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
}
