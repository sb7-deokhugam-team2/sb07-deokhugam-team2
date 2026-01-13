package com.deokhugam.domain.comment.controller;

import com.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.domain.comment.dto.request.CommentSearchCondition;
import com.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.dto.response.CursorPageResponseCommentDto;
import com.deokhugam.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
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
            @ModelAttribute CommentSearchCondition commentSearchCondition
    ){
        return null;
    }

    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @ModelAttribute CommentCreateRequest commentCreateRequest
    ){
        return null;
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getComment(
            @PathVariable UUID commentId
    ){
        return null;
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> logicalDelete(
            @PathVariable UUID commentId,
            @RequestHeader(value = "Deokhugam-Request-Id") UUID userId
    ){
        return null;
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable UUID commentId,
            @RequestHeader(value = "Deokhugam-Request-Id") UUID userId,
            @ModelAttribute CommentUpdateRequest commentUpdateRequest
    ){
        return null;
    }

    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> physicalDelete(
            @PathVariable UUID commentId,
            @RequestHeader(value = "Deokhugam-Request-Id") UUID userId
    ){
        return null;
    }
}
