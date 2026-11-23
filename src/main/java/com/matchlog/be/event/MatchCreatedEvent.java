package com.matchlog.be.event;

import java.time.LocalDateTime;
import java.util.List;

public record MatchCreatedEvent(
        Long matchId,
        Long teamId,
        String teamName,
        String opponent,
        LocalDateTime matchDate,
        List<Long> memberUserIds) {}
