package com.deokhugam.domain.comment.repository;

import com.deokhugam.domain.comment.dto.request.CommentSearchCondition;
import com.deokhugam.domain.comment.entity.Comment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentCustomRepository {

    List<Comment> searchComments(CommentSearchCondition condition);

    Optional<Comment> findWithUser(UUID commentId);

    Optional<Comment> findWithUserAndReview(UUID commentId);

    long getCountByReviewId(UUID reviewId);
}
