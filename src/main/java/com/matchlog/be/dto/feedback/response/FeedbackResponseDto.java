package com.matchlog.be.dto.feedback.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.domain.feedback.PersonalFeedback;
import com.matchlog.be.domain.feedback.TeamFeedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponseDto {

    private Long feedbackId;
    private Long matchId;
    private String scope;
    private Long playerId;
    private String content;
    private Integer rating;
    private String pros;
    private String cons;
    private List<String> tags;
    private Boolean isVisible;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static FeedbackResponseDto from(TeamFeedback feedback) {
        return FeedbackResponseDto.builder()
                .feedbackId(feedback.getId())
                .matchId(feedback.getMatch().getId())
                .scope("team")
                .playerId(null)
                .content(feedback.getContent())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }

    public static FeedbackResponseDto from(PersonalFeedback feedback) {
        return FeedbackResponseDto.builder()
                .feedbackId(feedback.getId())
                .matchId(feedback.getMatch().getId())
                .scope("player")
                .playerId(feedback.getPlayer().getId())
                .content(feedback.getContent())
                .rating(feedback.getRating())
                .pros(feedback.getPros())
                .cons(feedback.getCons())
                .tags(feedback.getTags())
                .isVisible(feedback.isVisible())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }
}
