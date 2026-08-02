package com.matchlog.be.dto.match.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.constant.match.HomeAway;
import com.matchlog.be.constant.match.MatchType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMatchRequestDto {

    private String opponent;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime matchDate;

    private String location;
    private HomeAway homeAway;
    private MatchType matchType;
    private Integer scoreHome;
    private Integer scoreAway;
    private Boolean isFinished;
}
