package com.deokhugam.domain.comment.service;

import com.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.domain.comment.dto.request.CommentSearchCondition;
import com.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.dto.response.CursorPageResponseCommentDto;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.exception.CommentNotFound;
import com.deokhugam.domain.comment.exception.CommentUnauthorizedException;
import com.deokhugam.domain.comment.repository.CommentRepository;
import com.deokhugam.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;

    @Override
    public CursorPageResponseCommentDto findContents(CommentSearchCondition commentSearchCondition) {
        return null;
    }

    @Override
    public CommentDto createComment(CommentCreateRequest commentCreateRequest) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto findComment(UUID commentId) {
        return commentRepository.findCommentDto(commentId).orElseThrow(() -> new CommentNotFound(ErrorCode.COMMENT_NOT_FOUND));
    }

    @Override
    @Transactional
    public void logicalDelete(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findWithUser(commentId).orElseThrow(() -> new CommentNotFound(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.isAuthor(userId)){
            throw new CommentUnauthorizedException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        comment.delete();
    }

    @Override
    @Transactional
    public CommentDto updateComment(UUID commentId, UUID userId, CommentUpdateRequest commentUpdateRequest) {
        Comment comment = commentRepository.findWithUserAndReview(commentId).orElseThrow(() -> new CommentNotFound(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.isAuthor(userId)){
            throw new CommentUnauthorizedException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        comment.updateContent(commentUpdateRequest.content());
        return CommentDto.from(comment);
    }

    @Override
    @Transactional
    public void physicalDelete(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findWithUser(commentId).orElseThrow(() -> new CommentNotFound(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.isAuthor(userId)){
            throw new CommentUnauthorizedException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        commentRepository.delete(comment);
    }
}
