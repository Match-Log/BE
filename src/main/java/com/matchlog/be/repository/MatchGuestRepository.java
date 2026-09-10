package com.matchlog.be.repository;

import com.matchlog.be.domain.match.MatchGuest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchGuestRepository extends JpaRepository<MatchGuest, Long> {

    List<MatchGuest> findByMatch_Id(Long matchId);
}
