package com.deokhugam.deokhugam.review.integration.service;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.repository.CommentRepository;
import com.deokhugam.domain.review.dto.request.CursorPageRequest;
import com.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.domain.review.dto.request.ReviewSearchCondition;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.enums.ReviewOrderBy;
import com.deokhugam.domain.review.enums.SortDirection;
import com.deokhugam.domain.review.exception.ReviewAccessDeniedException;
import com.deokhugam.domain.review.exception.ReviewAlreadyExistsException;
import com.deokhugam.domain.review.exception.ReviewInvalidException;
import com.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.review.service.ReviewService;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class ReviewServiceIntegrationTest {
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private ReviewService reviewService;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("성공 - 리뷰 생성")
    void createReview_Success() {
        // given
        User user = userRepository.save(
                User.create("test@gmail.com", "nickname", "password123!")
        );
        Book book = bookRepository.save(
                Book.create(
                        "title",
                        "author",
                        "123456789",
                        LocalDate.now(),
                        "publisher",
                        "thumbnailUrl",
                        "description")
        );
        Review review = Review.create(4.0, "리뷰 생성", book, user);

        // when
        Review savedReview = reviewRepository.save(review);

        em.flush();
        em.clear();

        // then
        Optional<Review> result = reviewRepository.findById(savedReview.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedReview.getId());
        assertThat(result.get().getContent()).isEqualTo("리뷰 생성");
        assertThat(result.get().getRating()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("실패 - 동일한 책과 유저로 두 번째 리뷰 작성 불가")
    void createReview_Fail_DuplicateReview() {
        // given
        User user = userRepository.save(
                User.create("test@gmail.com", "nickname", "password123!")
        );
        Book book = bookRepository.save(
                Book.create(
                        "title",
                        "author",
                        "123456789",
                        LocalDate.now(),
                        "publisher",
                        "thumbnailUrl",
                        "description")
        );
        Review firstReview = Review.create(4.0, "첫 번째 리뷰 생성", book, user);
        reviewRepository.save(firstReview);

        em.flush();
        em.clear();

        ReviewCreateRequest secondReviewRequest = new ReviewCreateRequest(
                book.getId(),
                user.getId(),
                3.0,
                "두 번째 리뷰 생성"
        );


        // when & then
        assertThrows(ReviewAlreadyExistsException.class, () -> {
            reviewService.createReview(secondReviewRequest);
        });
    }
    
    @Test
    @DisplayName("목록 조회 성공 - 첫 페이지로 next값이 계산된다.")
    void searchReview_success_firstPage_calculate_nextValue() throws InterruptedException {
        // given
        UUID requestUserId = UUID.randomUUID();

        User user = userRepository.save(
                User.create("user@naver.com", "nickname", "password123!")
        );
        Book book = bookRepository.save(
                Book.create(
                        "title",
                        "author",
                        "1325324234-333",
                        LocalDate.now(),
                        "publisher",
                        "thumbnailUrl",
                        "description")
        );
        Review review1 = reviewRepository.save(
                Review.create(5.0, "content1", book, user)
        );
        Thread.sleep(1000);
        Review review2 = reviewRepository.save(
                Review.create(4.0, "content2", book, user)
        );
        Thread.sleep(1000);
        Review review3 = reviewRepository.save(
                Review.create(3.0, "content3", book, user)
        );

        ReviewSearchCondition condition = new ReviewSearchCondition(
                null,
                null,
                null
        );

        CursorPageRequest firstPageRequest = new CursorPageRequest(
                ReviewOrderBy.CREATED_AT,
                SortDirection.DESC,
                null,
                null,
                2
        );

        // when
        ReviewPageResponseDto firstPage = reviewService.searchReviews(condition, firstPageRequest, requestUserId);

        // then
        assertThat(firstPage).isNotNull();
        assertThat(firstPage.content()).hasSize(2);
        assertThat(firstPage.hasNext()).isTrue();

        assertThat(firstPage.content().get(0).content()).isEqualTo("content3");
        assertThat(firstPage.content().get(1).content()).isEqualTo("content2");

        assertThat(firstPage.nextCursor()).isNotBlank();
        assertThat(firstPage.nextAfter()).isNull();

        CursorPageRequest secondPageRequest = new CursorPageRequest(
                ReviewOrderBy.CREATED_AT,
                SortDirection.DESC,
                firstPage.nextCursor(),
                null,
                2
        );

        ReviewPageResponseDto secondPage = reviewService.searchReviews(condition, secondPageRequest, requestUserId);

        assertThat(secondPage).isNotNull();
        assertThat(secondPage.content()).hasSize(1);
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.content().get(0).content()).isEqualTo("content1");
    }

    @Test
    @DisplayName("상세 조회 성공 - commentCount/likedByMe가 계산된다.") // TODO: likedByMe 확인
    void getReview_success_commentCountAndLikedByMe_Calculate() {
        // given
        UUID requestUserId = UUID.randomUUID();

        User author = userRepository.save(
                User.create("user@naver.com", "nickname", "password123!")
        );
        User requester = userRepository.save(
                User.create("requester@naver.com", "nickname2", "password123!")
        );
        Book book = bookRepository.save(
                Book.create(
                        "title",
                        "author",
                        "1325324234-333",
                        LocalDate.now(),
                        "publisher",
                        "thumbnailUrl",
                        "description")
        );
        Review review = reviewRepository.save(
                Review.create(5.0, "content1", book, author)
        );

        Comment comment1 = commentRepository.save(
                Comment.create("comment1", requester, review)
        );
        Comment comment2 = commentRepository.save(
                Comment.create("comment2", requester, review)
        );

        comment2.delete();

        // when
        ReviewDto result = reviewService.getReview(requester.getId(), review.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(review.getId());

        assertThat(result.likedByMe()).isFalse();

        assertThat(result.commentCount()).isEqualTo(1L);

    }

    @Test
    @DisplayName("목록 조회 실패 - rating정렬 시 after가 없으면 Exception Error")
    void searchReview_fail_ratingAfterMissing() {
        // given
        UUID requestUserId = UUID.randomUUID();

        ReviewSearchCondition condition = new ReviewSearchCondition(
                null,
                null,
                null
                );

        CursorPageRequest pageRequest = new CursorPageRequest(
                ReviewOrderBy.RATING,
                SortDirection.DESC,
                "5.0",
                null,
                20
        );

        // when & then
        assertThrows(ReviewInvalidException.class,
                () -> reviewService.searchReviews(condition, pageRequest, requestUserId));

    }

    @Test
    @DisplayName("상세 조회 실패 - 존재하지 않는 리뷰면 NOT FOUND 예외")
    void getReview_fail_notFound() {
        // given
        UUID requestUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();


        // when & then
        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.getReview(requestUserId, reviewId ));


    }

    @Test
    @DisplayName("성공: 리뷰 수정")
    void updateReview_Success() {
        // given
        User user = userRepository.save(
                User.create("test@gmail.com", "nickname", "password123!")
        );
        Book book = bookRepository.save(
                Book.create(
                        "title",
                        "author",
                        "123456789",
                        LocalDate.now(),
                        "publisher",
                        "thumbnailUrl",
                        "description")
        );
        Review review = reviewRepository.save(
                Review.create(4.0, "리뷰 내용", book, user)
        );

        em.flush();
        em.clear();

        // when
        Review savedReview = reviewRepository.findById(review.getId()).orElseThrow();
        savedReview.update(3.0, "리뷰 수정 내용");
        reviewRepository.save(savedReview);

        em.flush();
        em.clear();

        // then
        Review updatedReview = reviewRepository.findById(savedReview.getId()).orElseThrow();
        assertThat(updatedReview.getContent()).isEqualTo("리뷰 수정 내용");
        assertThat(updatedReview.getRating()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 리뷰를 수정하려고 할 때 예외 발생")
    void updateReview_Fail_NotFound() {
        // given
        UUID invalidReviewId = UUID.randomUUID();

        em.flush();
        em.clear();

        // when & then
        assertThrows(ReviewNotFoundException.class, () -> {
            Review review = reviewRepository.findById(invalidReviewId).orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
            review.update(3.0, "리뷰 수정 내용");

            reviewRepository.save(review);
        });
    }

    @Test
    @DisplayName("성공: 리뷰 논리 삭제")
    void softDeleteReview_Success() {
        // given
        User user = userRepository.save(
                User.create("test@gmail.com", "nickname", "password123!")
        );
        Book book = bookRepository.save(
                Book.create(
                        "title",
                        "author",
                        "123456789",
                        LocalDate.now(),
                        "publisher",
                        "thumbnailUrl",
                        "description")
        );
        Review review = Review.create(4.0, "리뷰 생성", book, user);
        Review savedReview = reviewRepository.save(review);

        em.flush();
        em.clear();

        // when
        reviewService.softDeleteReview(savedReview.getId(), user.getId());

        // then
        Optional<Review> result = reviewRepository.findById(savedReview.getId());
        assertThat(result).isPresent();
        assertThat(result.get().isDeleted()).isTrue();
    }

    @Test
    @DisplayName("실패 - 권한이 없는 사용자가 리뷰를 삭제하려고 할 때 예외 발생")
    void softDeleteReview_Fail_AccessDenied() {
        User reviewer = userRepository.save(User.create("test@gmail.com", "nickname", "password123!")); // 리뷰 작성자
        User unauthorizedUser = userRepository.save(
                User.create("unauthorized@gmail.com", "unauthorized", "password123!")
        );

        Book book = bookRepository.save(
                Book.create(
                        "title",
                        "author",
                        "123456789",
                        LocalDate.now(),
                        "publisher",
                        "thumbnailUrl",
                        "description")
        );

        Review review = reviewRepository.save(Review.create(5.0, "리뷰 내용", book, reviewer));

        em.flush();
        em.clear();

        // when & then
        assertThrows(ReviewAccessDeniedException.class, () -> {
            reviewService.softDeleteReview(review.getId(), unauthorizedUser.getId());
        });
    }

    @Test
    @DisplayName("성공: 리뷰 물리 삭제")
    void hardDeleteReview_Success() {
        // given
        User user = userRepository.save(
                User.create("test@gmail.com", "nickname", "password123!")
        );
        Book book = bookRepository.save(
                Book.create(
                        "title",
                        "author",
                        "123456789",
                        LocalDate.now(),
                        "publisher",
                        "thumbnailUrl",
                        "description")
        );

        // when
        Review review = Review.create(4.0, "리뷰 생성", book, user);
        Review savedReview = reviewRepository.save(review);

        em.flush();
        em.clear();

        UUID reviewId = savedReview.getId();
        assertThat(reviewId).isNotNull();

        // when
        reviewRepository.deleteById(reviewId);

        em.flush();
        em.clear();

        // then
        Optional<Review> result = reviewRepository.findById(reviewId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("실패: 존재하지 않는 리뷰 물리 삭제 테스트")
    void hardDeleteReview_Fail_NotFound() {
        // given
        UUID nonExistentReviewId = UUID.randomUUID();

        // when & then
        assertThrows(ReviewNotFoundException.class, () -> {
            Review review = reviewRepository.findById(nonExistentReviewId).orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

            reviewRepository.delete(review);
            em.flush();
        });
    }
}
