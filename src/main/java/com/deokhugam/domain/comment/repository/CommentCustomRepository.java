package com.deokhugam.domain.comment.repository;

import com.deokhugam.domain.comment.dto.request.CommentSearchCondition;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.entity.Comment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentCustomRepository {

    List<Comment> searchComments(CommentSearchCondition condition);

    Optional<CommentDto> findCommentDto(UUID commentId);
}
