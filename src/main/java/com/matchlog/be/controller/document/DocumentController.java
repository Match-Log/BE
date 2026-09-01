package com.matchlog.be.controller.document;

import com.matchlog.be.dto.document.request.CreateDocumentRequestDto;
import com.matchlog.be.dto.document.request.UpdateDocumentRequestDto;
import com.matchlog.be.dto.document.response.CreateDocumentResponseDto;
import com.matchlog.be.dto.document.response.DocumentListItemResponseDto;
import com.matchlog.be.dto.document.response.DocumentResponseDto;
import com.matchlog.be.dto.document.response.PinDocumentResponseDto;
import com.matchlog.be.dto.document.response.UpdateDocumentResponseDto;
import com.matchlog.be.service.document.DocumentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<CreateDocumentResponseDto> createDocument(
            @AuthenticationPrincipal Long userId, @RequestBody CreateDocumentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.createDocument(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<DocumentListItemResponseDto>> getDocuments(
            @AuthenticationPrincipal Long userId, @RequestParam Long teamId) {
        return ResponseEntity.ok(documentService.getDocuments(userId, teamId));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<DocumentResponseDto> getDocument(
            @AuthenticationPrincipal Long userId, @PathVariable Long boardId) {
        return ResponseEntity.ok(documentService.getDocument(userId, boardId));
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<UpdateDocumentResponseDto> updateDocument(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long boardId,
            @RequestBody UpdateDocumentRequestDto request) {
        return ResponseEntity.ok(documentService.updateDocument(userId, boardId, request));
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteDocument(
            @AuthenticationPrincipal Long userId, @PathVariable Long boardId) {
        documentService.deleteDocument(userId, boardId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{boardId}/pin")
    public ResponseEntity<PinDocumentResponseDto> togglePin(
            @AuthenticationPrincipal Long userId, @PathVariable Long boardId) {
        return ResponseEntity.ok(documentService.togglePin(userId, boardId));
    }
}
