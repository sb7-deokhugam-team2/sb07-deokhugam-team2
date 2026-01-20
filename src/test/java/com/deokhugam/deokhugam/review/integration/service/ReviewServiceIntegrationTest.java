package com.deokhugam.deokhugam.review.integration.service;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.repository.CommentRepository;
import com.deokhugam.domain.review.dto.request.CursorPageRequest;
import com.deokhugam.domain.review.dto.request.ReviewSearchCondition;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.enums.ReviewOrderBy;
import com.deokhugam.domain.review.enums.SortDirection;
import com.deokhugam.domain.review.exception.ReviewInvalidException;
import com.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.review.service.ReviewService;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
}
