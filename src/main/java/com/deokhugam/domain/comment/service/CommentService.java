package com.deokhugam.domain.comment.service;

import com.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.domain.comment.dto.request.CommentSearchCondition;
import com.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.dto.response.CursorPageResponseCommentDto;

import java.util.UUID;

public interface CommentService {

    CursorPageResponseCommentDto findContents(CommentSearchCondition commentSearchCondition);

    CommentDto createComment(CommentCreateRequest commentCreateRequest);

    CommentDto findComment(UUID commentId);

    void logicalDelete(UUID commentId, UUID userId);

    CommentDto updateComment(UUID commentId, UUID userId, CommentUpdateRequest commentUpdateRequest);

    void physicalDelete(UUID commentId, UUID userId);
}
