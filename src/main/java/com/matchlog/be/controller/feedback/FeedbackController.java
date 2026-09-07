package com.matchlog.be.controller.feedback;

import com.matchlog.be.dto.feedback.request.SavePersonalFeedbackRequestDto;
import com.matchlog.be.dto.feedback.request.SaveTeamFeedbackRequestDto;
import com.matchlog.be.dto.feedback.response.FeedbackResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.FeedbackErrorCode;
import com.matchlog.be.service.feedback.FeedbackService;
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
@RequestMapping("/api/v1/matches/{matchId}/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<?> getFeedbacks(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @RequestParam(required = false) String scope) {
        if ("team".equals(scope)) {
            return ResponseEntity.ok(feedbackService.getTeamFeedback(userId, matchId));
        }
        throw new CustomException(FeedbackErrorCode.INVALID_SCOPE);
    }

    @PutMapping
    public ResponseEntity<FeedbackResponseDto> saveTeamFeedback(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @RequestParam(required = false) String scope,
            @RequestBody SaveTeamFeedbackRequestDto request) {
        if (!"team".equals(scope)) {
            throw new CustomException(FeedbackErrorCode.INVALID_SCOPE);
        }
        return ResponseEntity.ok(feedbackService.saveTeamFeedback(userId, matchId, request));
    }

    @GetMapping("/players")
    public ResponseEntity<List<FeedbackResponseDto>> getAllPersonalFeedbacks(
            @AuthenticationPrincipal Long userId, @PathVariable Long matchId) {
        return ResponseEntity.ok(feedbackService.getAllPersonalFeedbacks(userId, matchId));
    }

    @GetMapping("/players/{playerId}")
    public ResponseEntity<FeedbackResponseDto> getPersonalFeedback(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @PathVariable Long playerId) {
        return ResponseEntity.ok(feedbackService.getPersonalFeedback(userId, matchId, playerId));
    }

    @PutMapping("/players/{playerId}")
    public ResponseEntity<FeedbackResponseDto> savePersonalFeedback(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchId,
            @PathVariable Long playerId,
            @RequestBody SavePersonalFeedbackRequestDto request) {
        return ResponseEntity.ok(
                feedbackService.savePersonalFeedback(userId, matchId, playerId, request));
    }
}
