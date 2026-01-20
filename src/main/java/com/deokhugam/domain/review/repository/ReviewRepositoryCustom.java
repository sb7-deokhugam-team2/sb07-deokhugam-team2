package com.deokhugam.domain.review.repository;

import com.deokhugam.domain.review.dto.request.CursorPageRequest;
import com.deokhugam.domain.review.dto.request.ReviewSearchCondition;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepositoryCustom {
    ReviewPageResponseDto search(
            ReviewSearchCondition condition, CursorPageRequest pageRequest, UUID requestId);

    Optional<ReviewDto> findDetail(UUID reviewId, UUID requestUserId);
}
