package com.matchlog.be.dto.participation.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignKickerRequestDto {

    private Boolean isCaptain;
    private Boolean isPkTaker;
    private Boolean isFkRight;
    private Boolean isFkLeft;
    private Boolean isCkRight;
    private Boolean isCkLeft;
}
