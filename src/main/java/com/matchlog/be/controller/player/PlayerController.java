package com.matchlog.be.controller.player;

import com.matchlog.be.dto.player.request.RegisterPlayerRequestDto;
import com.matchlog.be.dto.player.response.PlayerProfileResponseDto;
import com.matchlog.be.dto.player.response.RegisterPlayerResponseDto;
import com.matchlog.be.service.player.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    public ResponseEntity<RegisterPlayerResponseDto> registerPlayer(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RegisterPlayerRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playerService.registerPlayer(userId, request));
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerProfileResponseDto> getPlayerProfile(@PathVariable Long playerId) {
        return ResponseEntity.ok(playerService.getPlayerProfile(playerId));
    }
}
