package com.matchlog.be.controller.vote;

import com.matchlog.be.dto.vote.request.SubmitMvpVoteRequestDto;
import com.matchlog.be.dto.vote.response.MvpVoteStatusResponseDto;
import com.matchlog.be.service.vote.MvpVoteService;
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
@RequestMapping("/api/v1/matches/{matchId}/mvp-votes")
@RequiredArgsConstructor
public class MvpVoteController {

    private final MvpVoteService mvpVoteService;

    @PutMapping
    public ResponseEntity<MvpVoteStatusResponseDto> saveMvpVote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @RequestBody SubmitMvpVoteRequestDto request) {
        return ResponseEntity.ok(mvpVoteService.saveMvpVote(userId, matchId, request));
    }

    @GetMapping
    public ResponseEntity<MvpVoteStatusResponseDto> getMvpVoteStatus(
            @AuthenticationPrincipal Long userId, @PathVariable Long matchId) {
        return ResponseEntity.ok(mvpVoteService.getMvpVoteStatus(userId, matchId));
    }
}