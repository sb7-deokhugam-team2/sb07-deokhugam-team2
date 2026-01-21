package com.deokhugam.domain.poweruser.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class CursorPageResponsePowerUserDto {
    private List<PowerUserDto> content;
    private String nextCursor;
    private Instant nextAfter;
    private Integer size;
    private Long totalElements;
    private boolean hasNext;

    public static CursorPageResponsePowerUserDto from(
            List<PowerUserDto> content,
            String nextCursor,
            Instant nextAfter,
            Integer size,
            Long totalElements,
            boolean hasNext
    ) {
        return new CursorPageResponsePowerUserDto(
                content, nextCursor, nextAfter, size, totalElements, hasNext
        );
    }
}
