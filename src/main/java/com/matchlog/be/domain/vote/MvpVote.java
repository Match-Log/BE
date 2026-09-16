package com.matchlog.be.domain.vote;

import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "MVP_VOTE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MvpVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matchId", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voterId", nullable = false)
    private Player voter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "votedPlayerId", nullable = false)
    private Player votedPlayer;

    private LocalDateTime votedAt;

    public static MvpVote create(Match match, Player voter, Player votedPlayer) {
        return MvpVote.builder()
                .match(match)
                .voter(voter)
                .votedPlayer(votedPlayer)
                .votedAt(LocalDateTime.now())
                .build();
    }

    public void changeVotedPlayer(Player votedPlayer) {
        this.votedPlayer = votedPlayer;
        this.votedAt = LocalDateTime.now();
    }
}
