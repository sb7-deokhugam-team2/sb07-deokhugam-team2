package com.deokhugam.deokhugam.likedreview.integration.service;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.likedreview.dto.response.LikedReviewDto;
import com.deokhugam.domain.likedreview.repository.LikedReviewRepository;
import com.deokhugam.domain.likedreview.service.LikedReviewService;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class LikedReviewServiceIntegrationTest {
    @Autowired
    EntityManager em;

    @Autowired
    LikedReviewService likedReviewService;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    LikedReviewRepository likedReviewRepository;

    @Test
    @DisplayName("좋아요 성공 - 연속적으로 toggle을 한다.")
    void likedReview_likedCount_toggle_continuously() {
        // given
        User user = userRepository.save(User.create("user@naver.com", "nickname", "password123!"));
        Book book = bookRepository.save(Book.create(
                "title",
                "author",
                "234234-423432",
                LocalDate.now(),
                "publisher",
                "thumbnailUrl",
                "description"
        ));
        Review review = reviewRepository.save(Review.create(4.5, "content", book, user));

        UUID reviewId = review.getId();
        UUID userId = user.getId();

        LikedReviewDto like1 = likedReviewService.toggleLike(reviewId, userId);
        em.flush();
        em.clear();
        Review afterReview1 = reviewRepository.findById(reviewId).orElseThrow();

        LikedReviewDto like2 = likedReviewService.toggleLike(reviewId, userId);
        em.flush();
        em.clear();
        Review afterReview2 = reviewRepository.findById(reviewId).orElseThrow();

        LikedReviewDto like3 = likedReviewService.toggleLike(reviewId, userId);
        em.flush();
        em.clear();
        Review afterReview3 = reviewRepository.findById(reviewId).orElseThrow();

        // then
        assertThat(like1.liked()).isTrue();
        assertThat(afterReview1.getLikedCount()).isEqualTo(1);

        assertThat(like2.liked()).isFalse();
        assertThat(afterReview2.getLikedCount()).isEqualTo(0);

        assertThat(like3.liked()).isTrue();
        assertThat(afterReview3.getLikedCount()).isEqualTo(1);

        assertThat(likedReviewRepository.findByReviewIdAndUserId(reviewId, userId)).isPresent();
    }
}
