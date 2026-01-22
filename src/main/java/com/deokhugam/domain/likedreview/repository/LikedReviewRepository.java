package com.deokhugam.domain.likedreview.repository;

import com.deokhugam.domain.likedreview.entity.LikedReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LikedReviewRepository extends JpaRepository<LikedReview, UUID> {
    Optional<LikedReview> findByReviewIdAndUserId(UUID reviewId, UUID userId);
}
