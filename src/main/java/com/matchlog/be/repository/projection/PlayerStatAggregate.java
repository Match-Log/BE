package com.matchlog.be.repository.projection;

public interface PlayerStatAggregate {

    Long getMatchCount();

    Long getGoals();

    Long getAssists();

    Long getSaves();

    Long getCleanSheetCount();

    Long getMvpCount();
}
