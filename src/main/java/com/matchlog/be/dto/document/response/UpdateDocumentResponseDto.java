package com.matchlog.be.dto.document.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.domain.document.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDocumentResponseDto {

    private Long boardId;
    private String title;
    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static UpdateDocumentResponseDto from(Document document) {
        return UpdateDocumentResponseDto.builder()
                .boardId(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
