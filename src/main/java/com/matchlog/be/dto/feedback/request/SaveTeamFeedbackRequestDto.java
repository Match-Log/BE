package com.matchlog.be.dto.feedback.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveTeamFeedbackRequestDto {

    private String content;
}
