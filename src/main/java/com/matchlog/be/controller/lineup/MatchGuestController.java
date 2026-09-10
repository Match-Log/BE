package com.matchlog.be.controller.lineup;

import com.matchlog.be.dto.lineup.request.CreateMatchGuestRequestDto;
import com.matchlog.be.dto.lineup.response.MatchGuestResponseDto;
import com.matchlog.be.service.lineup.MatchGuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matches/{matchId}/guests")
@RequiredArgsConstructor
public class MatchGuestController {

    private final MatchGuestService matchGuestService;

    @PostMapping
    public ResponseEntity<MatchGuestResponseDto> createGuest(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @Valid @RequestBody CreateMatchGuestRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matchGuestService.createGuest(userId, matchId, request));
    }

    @DeleteMapping("/{guestId}")
    public ResponseEntity<Void> deleteGuest(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @PathVariable Long guestId) {
        matchGuestService.deleteGuest(userId, matchId, guestId);
        return ResponseEntity.noContent().build();
    }
}
