package com.deokhugam.domain.comment.controller;

import com.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.domain.comment.dto.request.CommentSearchCondition;
import com.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.dto.response.CursorPageResponseCommentDto;
import com.deokhugam.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<CursorPageResponseCommentDto> getComments(
            @Valid @ModelAttribute CommentSearchCondition commentSearchCondition
    ){
        return null;
    }

    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @Valid @RequestBody CommentCreateRequest commentCreateRequest
    ){
        return null;
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getComment(
            @PathVariable UUID commentId
    ){
        CommentDto commentDto = commentService.findComment(commentId);
        return ResponseEntity.ok().body(commentDto);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> logicalDelete(
            @PathVariable UUID commentId,
            @RequestHeader(value = "Deokhugam-Request-Id") UUID userId
    ){
        commentService.logicalDelete(commentId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable UUID commentId,
            @RequestHeader(value = "Deokhugam-Request-Id") UUID userId,
            @Valid @RequestBody CommentUpdateRequest commentUpdateRequest
    ){
        CommentDto commentDto = commentService.updateComment(commentId, userId, commentUpdateRequest);
        return ResponseEntity.ok().body(commentDto);
    }

    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> physicalDelete(
            @PathVariable UUID commentId,
            @RequestHeader(value = "Deokhugam-Request-Id") UUID userId
    ){
        commentService.physicalDelete(commentId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
