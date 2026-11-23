package com.matchlog.be.event;

import java.util.List;

public record PlayerJoinedTeamEvent(
        Long teamId, String teamName, String newPlayerName, List<Long> existingMemberUserIds) {}
