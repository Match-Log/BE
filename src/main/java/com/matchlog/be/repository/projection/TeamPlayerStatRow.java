package com.matchlog.be.repository.projection;

public interface TeamPlayerStatRow {

    Long getPlayerId();

    Long getMatchCount();

    Long getGoals();

    Long getAssists();

    Long getSaves();

    Long getCleanSheetCount();

    Long getMvpCount();
}
