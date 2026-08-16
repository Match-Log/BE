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
public class DocumentListItemResponseDto {

    private Long boardId;
    private Long teamId;
    private Long playerId;
    private String playerName;
    private String title;
    private Long matchId;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static DocumentListItemResponseDto from(Document document) {
        return DocumentListItemResponseDto.builder()
                .boardId(document.getId())
                .teamId(document.getTeam().getId())
                .playerId(document.getPlayer().getId())
                .playerName(document.getPlayer().getUser().getName())
                .title(document.getTitle())
                .matchId(document.getMatch() != null ? document.getMatch().getId() : null)
                .createdAt(document.getCreatedAt())
                .build();
    }
}
