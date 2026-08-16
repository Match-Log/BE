package com.matchlog.be.domain.feedback;

import com.matchlog.be.domain.common.BaseTimeEntity;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "PERSONAL_FEEDBACK")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PersonalFeedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matchId", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerId", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coachId", nullable = false)
    private Player coach;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String pros;

    @Column(columnDefinition = "TEXT")
    private String cons;

    @Convert(converter = TagsConverter.class)
    @Column(length = 500)
    private List<String> tags;

    @Column(nullable = false)
    @Builder.Default
    private boolean isVisible = true;

    public static PersonalFeedback create(
            Match match,
            Player player,
            Player coach,
            String content,
            Integer rating,
            String pros,
            String cons,
            List<String> tags,
            boolean isVisible) {
        return PersonalFeedback.builder()
                .match(match)
                .player(player)
                .coach(coach)
                .content(content)
                .rating(rating)
                .pros(pros)
                .cons(cons)
                .tags(tags)
                .isVisible(isVisible)
                .build();
    }

    public void update(
            String content,
            Integer rating,
            String pros,
            String cons,
            List<String> tags,
            Boolean isVisible) {
        this.content = content;
        this.rating = rating;
        this.pros = pros;
        this.cons = cons;
        this.tags = tags;
        if (isVisible != null) {
            this.isVisible = isVisible;
        }
    }
}
