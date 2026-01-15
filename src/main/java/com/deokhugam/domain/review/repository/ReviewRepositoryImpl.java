package com.deokhugam.domain.review.repository;

import com.deokhugam.domain.review.dto.request.CursorPageRequest;
import com.deokhugam.domain.review.dto.request.ReviewSearchCondition;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom{
    @Override
    public ReviewPageResponseDto search(ReviewSearchCondition condition, CursorPageRequest pageRequest, UUID requestId) {
        return null;
    }
}
