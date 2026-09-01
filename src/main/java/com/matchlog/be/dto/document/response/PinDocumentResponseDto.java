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
public class PinDocumentResponseDto {

    private Long boardId;
    private boolean isPinned;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static PinDocumentResponseDto from(Document document) {
        return PinDocumentResponseDto.builder()
                .boardId(document.getId())
                .isPinned(document.isPinned())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
