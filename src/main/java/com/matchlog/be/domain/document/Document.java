package com.matchlog.be.domain.document;

import com.matchlog.be.constant.document.DocumentType;
import com.matchlog.be.domain.common.BaseTimeEntity;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "DOCUMENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Document extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teamId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerId", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matchId")
    private Match match;

    @Column(name = "isPinned", nullable = false)
    private boolean isPinned;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DocumentType documentType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public static Document create(
            Team team,
            Player player,
            Match match,
            DocumentType documentType,
            String title,
            String content,
            boolean isPinned) {
        return Document.builder()
                .team(team)
                .player(player)
                .match(match)
                .documentType(documentType)
                .title(title)
                .content(content)
                .isPinned(isPinned)
                .build();
    }

    public void updateContent(String title, String content) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
    }

    public void pin() {
        this.isPinned = true;
    }

    public void unpin() {
        this.isPinned = false;
    }
}
