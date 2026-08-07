package com.matchlog.be.dto.document.request;

import com.matchlog.be.constant.document.DocumentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateDocumentRequestDto {

    private Long teamId;
    private DocumentType documentType;
    private String title;
    private String content;

    @Builder.Default
    private boolean isPinned = false;
}
