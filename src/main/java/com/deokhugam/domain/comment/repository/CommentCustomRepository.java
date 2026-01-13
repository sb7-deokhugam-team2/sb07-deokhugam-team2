package com.deokhugam.domain.comment.repository;

import com.deokhugam.domain.comment.entity.Comment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CommentCustomRepository {

    List<Comment> findCommentByCursor(
            UUID reviewId, String direction, String cursor, Instant after, Integer limit
    );

}
