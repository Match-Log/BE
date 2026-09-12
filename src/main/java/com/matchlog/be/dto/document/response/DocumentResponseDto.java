package com.matchlog.be.dto.document.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.domain.document.Document;
import com.matchlog.be.dto.comment.response.CommentResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponseDto {

    private Long boardId;
    private Long teamId;
    private Long playerId;
    private String playerName;
    private String title;
    private String content;
    private Long matchId;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    private List<CommentResponseDto> comments;

    public static DocumentResponseDto from(Document document, List<CommentResponseDto> comments) {
        return DocumentResponseDto.builder()
                .boardId(document.getId())
                .teamId(document.getTeam().getId())
                .playerId(document.getPlayer().getId())
                .playerName(document.getPlayer().getUser().getName())
                .title(document.getTitle())
                .content(document.getContent())
                .matchId(document.getMatch() != null ? document.getMatch().getId() : null)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .comments(comments)
                .build();
    }
}
