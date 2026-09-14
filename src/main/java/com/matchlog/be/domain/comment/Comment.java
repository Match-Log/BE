package com.matchlog.be.domain.comment;

import com.matchlog.be.domain.common.BaseTimeEntity;
import com.matchlog.be.domain.document.Document;
import com.matchlog.be.domain.player.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "COMMENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documentId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerId", nullable = false)
    private Player player;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public static Comment create(Document document, Player player, String content) {
        return Comment.builder().document(document).player(player).content(content).build();
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
