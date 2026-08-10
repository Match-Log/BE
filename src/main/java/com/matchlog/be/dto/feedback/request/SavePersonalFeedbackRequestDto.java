package com.matchlog.be.dto.feedback.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavePersonalFeedbackRequestDto {

    private String content;
    private Integer rating;
    private String pros;
    private String cons;
    private List<String> tags;
    private Boolean isVisible;
}
