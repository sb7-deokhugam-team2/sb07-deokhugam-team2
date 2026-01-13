package com.deokhugam.domain.comment.dto.response;

import com.deokhugam.domain.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CommentDto {
    private UUID id;
    private UUID reviewId;
    private UUID userId;
    private String userNickname;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;

    public static CommentDto from(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getReview().getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
