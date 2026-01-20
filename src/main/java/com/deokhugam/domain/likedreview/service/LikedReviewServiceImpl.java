package com.deokhugam.domain.likedreview.service;

import com.deokhugam.domain.likedreview.dto.response.LikedReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LikedReviewServiceImpl implements LikedReviewService {
    @Override
    public LikedReviewDto toggleLike(UUID reviewId, UUID userId) {
        return null;
    }
}
