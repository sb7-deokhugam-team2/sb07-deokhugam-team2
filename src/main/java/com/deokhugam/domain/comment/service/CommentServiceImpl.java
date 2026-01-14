package com.deokhugam.domain.comment.service;

import com.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.domain.comment.dto.request.CommentSearchCondition;
import com.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.dto.response.CursorPageResponseCommentDto;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.exception.CommentNotFound;
import com.deokhugam.domain.comment.exception.CommentUnauthorizedException;
import com.deokhugam.domain.comment.repository.CommentQueryRepository;
import com.deokhugam.domain.comment.repository.CommentRepository;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.exception.UserNotFoundException;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.exception.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final CommentQueryRepository commentQueryRepository;

    @Override
    public CursorPageResponseCommentDto findContents(CommentSearchCondition commentSearchCondition) {
        List<CommentDto> contents = commentRepository.searchComments(commentSearchCondition)
                .stream()
                .map(CommentDto::from)
                .collect(Collectors.toList());//20 -> 21 : hasNext=true

        boolean hasNext = contents.size() > commentSearchCondition.limit();
        if (contents.size() > commentSearchCondition.limit()) {
            contents.remove(contents.size() - 1);
        }

        String nextCursor = null;
        Instant nextAfter = null;

        if(!contents.isEmpty()){
            CommentDto lastItem = contents.get(contents.size() - 1);
            nextCursor = lastItem.getCreatedAt().toString();
            nextAfter = lastItem.getCreatedAt();
        }

        long totalElements = commentRepository.count();

        return CursorPageResponseCommentDto.from(
                contents,
                nextCursor,
                nextAfter,
                contents.size(),
                totalElements,
                hasNext
        );
    }

    @Override
    public CommentDto createComment(CommentCreateRequest commentCreateRequest) {
        //리뷰에 댓글 작성
        User userId = userRepository.findById(commentCreateRequest.userId())
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
        Review reviewId = reviewRepository.findById(commentCreateRequest.reviewId())
                .orElseThrow(() -> new EntityNotFoundException("요청한 리뷰 정보를 찾을 수 없습니다.")); //TODO: customError작성

        Comment comment = Comment.create(
                commentCreateRequest.content(),
                userId,
                reviewId
        );
        commentRepository.save(comment);

        return CommentDto.from(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto findComment(UUID commentId) {
        return commentQueryRepository.findCommentDto(commentId).orElseThrow(() -> new CommentNotFound(ErrorCode.COMMENT_NOT_FOUND));
    }

    @Override
    public void logicalDelete(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findWithUser(commentId).orElseThrow(() -> new CommentNotFound(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isAuthor(userId)) {
            throw new CommentUnauthorizedException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        comment.delete();
    }

    @Override
    public CommentDto updateComment(UUID commentId, UUID userId, CommentUpdateRequest commentUpdateRequest) {
        Comment comment = commentRepository.findWithUserAndReview(commentId).orElseThrow(() -> new CommentNotFound(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isAuthor(userId)) {
            throw new CommentUnauthorizedException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        comment.updateContent(commentUpdateRequest.content());
        return CommentDto.from(comment);
    }

    @Override
    public void physicalDelete(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findWithUser(commentId).orElseThrow(() -> new CommentNotFound(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isAuthor(userId)) {
            throw new CommentUnauthorizedException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        commentRepository.delete(comment);
    }
}
