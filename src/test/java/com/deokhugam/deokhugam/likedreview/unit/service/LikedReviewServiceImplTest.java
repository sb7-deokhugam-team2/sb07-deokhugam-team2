package com.deokhugam.deokhugam.likedreview.unit.service;

import com.deokhugam.domain.likedreview.dto.response.LikedReviewDto;
import com.deokhugam.domain.likedreview.entity.LikedReview;
import com.deokhugam.domain.likedreview.repository.LikedReviewRepository;
import com.deokhugam.domain.likedreview.service.LikedReviewServiceImpl;
import com.deokhugam.domain.notification.service.NotificationCreator;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikedReviewServiceImplTest {

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    LikedReviewRepository likedReviewRepository;

    @Mock
    NotificationCreator notificationCreator;

    @InjectMocks
    LikedReviewServiceImpl likedReviewService;

    @Test
    @DisplayName("좋아요 생성 시 likeCount가 1 증가한다.")
    void likedReview_create_success_plusLikedCount() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Review review = mock(Review.class);
        when(review.isDeleted()).thenReturn(false);

        User user = mock(User.class);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(likedReviewRepository.findByReviewIdAndUserId(reviewId, userId)).thenReturn(Optional.empty());

        LikedReview likedReview = mock(LikedReview.class);
        when(likedReviewRepository.saveAndFlush(any(LikedReview.class))).thenReturn(likedReview);

        // when
        LikedReviewDto dto = likedReviewService.toggleLike(reviewId, userId);

        // then
        assertThat(dto.liked()).isTrue();

        verify(likedReviewRepository).saveAndFlush(any(LikedReview.class));
        verify(notificationCreator).createNotification(likedReview);
        verify(reviewRepository).addLikedCount(reviewId, +1);

    }

    @Test
    @DisplayName("좋아요가 있을 시 toggle되고 likedCount가 -1 된다.")
    void likedReview_create_success_minusLikedCount() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Review review = mock(Review.class);
        when(review.isDeleted()).thenReturn(false);

        User user = mock(User.class);

        LikedReview likedReview = mock(LikedReview.class);
        when(likedReview.isLiked()).thenReturn(true);
        when(likedReview.toggle()).thenReturn(false);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(likedReviewRepository.findByReviewIdAndUserId(reviewId, userId)).thenReturn(Optional.of(likedReview));

        // when
        LikedReviewDto dto = likedReviewService.toggleLike(reviewId, userId);

        // then
        assertThat(dto.liked()).isFalse();
        verify(reviewRepository).addLikedCount(reviewId, -1);
        verify(notificationCreator, never()).createNotification((LikedReview) any());
    }
}

