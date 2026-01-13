package com.deokhugam.domain.comment.service;

import com.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.domain.comment.dto.request.CommentCursorRequest;
import com.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.dto.response.CursorPageResponseCommentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService{
    @Override
    public CursorPageResponseCommentDto findContents(CommentCursorRequest commentCursorRequest) {
        return null;
    }

    @Override
    public CommentDto createComment(CommentCreateRequest commentCreateRequest) {
        return null;
    }

    @Override
    public CommentDto findComment(UUID commentId) {
        return null;
    }

    @Override
    public void logicalDelete(UUID commentId, UUID userId) {

    }

    @Override
    public CommentDto updateComment(UUID commentId, UUID userId, CommentUpdateRequest commentUpdateRequest) {
        return null;
    }

    @Override
    public void physicalDelete(UUID commentId, UUID userId) {

    }
}
