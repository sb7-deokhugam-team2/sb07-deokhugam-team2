package com.deokhugam.domain.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class CursorPageResponseCommentDto {
    private List<CommentDto> content;
    private String nextCursor;
    private Instant nextAfter;
    private Integer size;
    private Long totalElements;
    private boolean hasNext;

    public static CursorPageResponseCommentDto from(
            List<CommentDto> content,
            String nextCursor,
            Instant nextAfter,
            Integer size,
            Long totalElements,
            boolean hasNext
    ) {
        return new CursorPageResponseCommentDto(
                content, nextCursor, nextAfter, size, totalElements, hasNext
        );
    }
}
