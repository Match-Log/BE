package com.matchlog.be.controller.vote;

import com.matchlog.be.dto.vote.request.SubmitVoteRequestDto;
import com.matchlog.be.dto.vote.response.VoteResponseDto;
import com.matchlog.be.dto.vote.response.VoteStatusResponseDto;
import com.matchlog.be.service.vote.VoteService;
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
@RequestMapping("/api/v1/matches/{matchId}/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PutMapping
    public ResponseEntity<VoteResponseDto> saveVote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @RequestBody SubmitVoteRequestDto request) {
        return ResponseEntity.ok(voteService.saveVote(userId, matchId, request));
    }

    @GetMapping
    public ResponseEntity<VoteStatusResponseDto> getVoteStatus(
            @AuthenticationPrincipal Long userId, @PathVariable Long matchId) {
        return ResponseEntity.ok(voteService.getVoteStatus(userId, matchId));
    }
}
