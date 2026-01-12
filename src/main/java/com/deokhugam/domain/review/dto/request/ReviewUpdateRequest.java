package com.deokhugam.domain.review.dto.request;

public record ReviewUpdateRequest(
        Double rating,
        String content
) {
}
