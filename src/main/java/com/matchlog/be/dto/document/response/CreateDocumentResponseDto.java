package com.matchlog.be.dto.document.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.domain.document.Document;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateDocumentResponseDto {

    private Long boardId;
    private Long teamId;
    private Long playerId;
    private String title;
    private String content;
    private boolean isPinned;
    private Long matchId;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static CreateDocumentResponseDto from(Document document) {
        return CreateDocumentResponseDto.builder()
                .boardId(document.getId())
                .teamId(document.getTeam().getId())
                .playerId(document.getPlayer().getId())
                .title(document.getTitle())
                .content(document.getContent())
                .isPinned(document.isPinned())
                .matchId(document.getMatch() != null ? document.getMatch().getId() : null)
                .createdAt(document.getCreatedAt())
                .build();
    }
}
