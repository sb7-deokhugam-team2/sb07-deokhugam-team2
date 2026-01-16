package com.deokhugam.domain.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Getter
public class CursorPageResponseNotificationDto {
    private List<NotificationDto> content;
    private String nextCursor;
    private Instant nextAfter;
    private Integer size;
    private Long totalElements;
    private boolean hasNext;

    public static CursorPageResponseNotificationDto from(
            List<NotificationDto> content,
            String nextCursor,
            Instant nextAfter,
            Integer size,
            Long totalElements,
            boolean hasNext
    ) {
        return new CursorPageResponseNotificationDto(
                content, nextCursor, nextAfter, size, totalElements, hasNext
        );
    }
}
