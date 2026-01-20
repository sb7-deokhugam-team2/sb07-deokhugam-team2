package com.deokhugam.deokhugam.review.integration.slice.jpa;

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
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.config.JpaAuditingConfig;
import com.deokhugam.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public class ReviewRepositorySliceTest {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("목록 조회 첫 페이지 - limit + 1 조회로 next값가 계산된다")
    void search_firstPage_Calculate_nextValue() {
        // given
        UUID requestUserId = UUID.randomUUID();

        User user = User.create("email@naver.com", "nickname", "password123!");
        Book book = Book.create(
                "title",
                "author",
                "1232132321312",
                LocalDate.now(),
                "publisher",
                "thumbnailUrl",
                "description");

        Review review1 = Review.create(5.0, "content1", book, user);
        Review review2 = Review.create(4.0, "content2", book, user);
        Review review3 = Review.create(3.0, "content3", book, user);

        userRepository.save(user);
        bookRepository.save(book);
        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        ReviewSearchCondition condition = new ReviewSearchCondition(
                null,
                null,
                null
        );

        CursorPageRequest pageRequest = new CursorPageRequest(
                ReviewOrderBy.CREATED_AT,
                SortDirection.DESC,
                null,
                null,
                2
        );

        // when
        ReviewPageResponseDto result = reviewRepository.search(condition, pageRequest, requestUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isTrue();

        assertThat(result.content().get(0).content()).isEqualTo("content3");
        assertThat(result.content().get(1).content()).isEqualTo("content2");

        assertThat(result.nextCursor()).isNotBlank();
        assertThat(Instant.parse(result.nextCursor())).isNotNull();

        assertThat(result.nextAfter()).isNull();

        assertThat(result.totalElements()).isEqualTo(3L);
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("목록 조회 다음 페이지 - cursor 기반으로 다음 페이지가 이어진다.")
    void search_nextPage() {
        // given
        UUID requestUserId = UUID.randomUUID();

        User user = User.create("email@naver.com", "nickname", "password123!");
        Book book = Book.create(
                "title",
                "author",
                "1232132321312",
                LocalDate.now(),
                "publisher",
                "thumbnailUrl",
                "description");

        Review review1 = Review.create(5.0, "content1", book, user);
        Review review2 = Review.create(4.0, "content2", book, user);
        Review review3 = Review.create(3.0, "content3", book, user);

        userRepository.save(user);
        bookRepository.save(book);
        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

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

        ReviewPageResponseDto firstPage = reviewRepository.search(condition, firstPageRequest, requestUserId);

        String nextCursor = firstPage.nextCursor();

        assertThat(nextCursor).isNotBlank();
        assertThat(firstPage.content()).hasSize(2);
        assertThat(firstPage.hasNext()).isTrue();

        CursorPageRequest secondPageRequest = new CursorPageRequest(
                ReviewOrderBy.CREATED_AT,
                SortDirection.DESC,
                nextCursor,
                null,
                2
        );

        // when
        ReviewPageResponseDto secondPage = reviewRepository.search(condition, secondPageRequest, requestUserId);

        // then
        assertThat(secondPage).isNotNull();
        assertThat(secondPage.content()).hasSize(1);
        assertThat(secondPage.hasNext()).isFalse();
        
        assertThat(secondPage.content().get(0).content()).isEqualTo("content1");

        assertThat(secondPage.nextCursor()).isNull();
        assertThat(secondPage.nextAfter()).isNull();

        assertThat(secondPage.totalElements()).isEqualTo(3L);
        assertThat(secondPage.size()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("상세 조회 - likedByMe=true, commentCount가 계산된다") // TODO: likedByMe 이후 다시 확인
    void findDetail_likedByMe_commentCount() {
        // given
        UUID requestUserId = UUID.randomUUID();

        User user = User.create("email@naver.com", "nickname", "password123!");
        User requester = User.create("email2@naver.com", "nickname2", "password123!");
        Book book = Book.create(
                "title",
                "author",
                "1232132321312",
                LocalDate.now(),
                "publisher",
                "thumbnailUrl",
                "description");
        Review review1 = Review.create(5.0, "content1", book, user);
        Comment comment1 = Comment.create("content1", requester, review1);
        Comment comment2 = Comment.create("content2", requester, review1);

        userRepository.save(user);
        userRepository.save(requester);
        bookRepository.save(book);
        reviewRepository.save(review1);
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        comment2.delete();
        commentRepository.save(comment2);


        // when
        Optional<ReviewDto> detail = reviewRepository.findDetail(review1.getId(), requester.getId());

        // then
        assertThat(detail).isPresent();

        ReviewDto reviewDto = detail.get();
        assertThat(reviewDto.id()).isEqualTo(review1.getId());
        assertThat(reviewDto.content()).isEqualTo("content1");

        assertThat(reviewDto.commentCount()).isEqualTo( 1L);

        // TODO: likedByMe 아직 작업 안되었음
        assertThat(reviewDto.likedByMe()).isFalse();
    }

    @Test
    @DisplayName("목록 조회 - rating DESC + after로 다음 페이지가 이어진다.")
    void search_ratingDESC_nextPage() throws Exception {
        // given
        UUID requestUserId = UUID.randomUUID();

        User user = User.create("email@naver.com", "nickname", "password123!");
        Book book = Book.create(
                "title",
                "author",
                "1232132321312",
                LocalDate.now(),
                "publisher",
                "thumbnailUrl",
                "description");

        Review review1 = Review.create(5.0, "content1", book, user);
        Review review2 = Review.create(5.0, "content2", book, user);
        Review review3 = Review.create(5.0, "content3", book, user);
        Review review4 = Review.create(4.0, "content4", book, user);

        userRepository.save(user);
        bookRepository.save(book);
        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);
        reviewRepository.save(review4);

        ReviewSearchCondition condition = new ReviewSearchCondition(
                null,
                null,
                null
        );

        CursorPageRequest firstPageRequest = new CursorPageRequest(
                ReviewOrderBy.RATING,
                SortDirection.DESC,
                null,
                null,
                2
        );

        // when
        ReviewPageResponseDto firstPage = reviewRepository.search(condition, firstPageRequest, requestUserId);

        // then
        assertThat(firstPage).isNotNull();
        assertThat(firstPage.content()).hasSize(2);
        assertThat(firstPage.hasNext()).isTrue();

        assertThat(firstPage.content().get(0).content()).isEqualTo("content3");
        assertThat(firstPage.content().get(1).content()).isEqualTo("content2");

        assertThat(firstPage.nextCursor()).isEqualTo("5.0");
        assertThat(firstPage.nextAfter()).isNotNull();

        // when
        CursorPageRequest secondPageRequest = new CursorPageRequest(
                ReviewOrderBy.RATING,
                SortDirection.DESC,
                firstPage.nextCursor(),
                firstPage.nextAfter(),
                2
        );

        ReviewPageResponseDto secondPage = reviewRepository.search(condition, secondPageRequest, requestUserId);

        // then
        assertThat(secondPage).isNotNull();
        assertThat(secondPage.content()).hasSize(2);
        assertThat(secondPage.hasNext()).isFalse();

        assertThat(secondPage.content().get(0).content()).isEqualTo("content1");
        assertThat(secondPage.content().get(1).content()).isEqualTo("content4");

        assertThat(secondPage.nextCursor()).isNull();
        assertThat(secondPage.nextAfter()).isNull();

        assertThat(secondPage.totalElements()).isEqualTo(4L);
        assertThat(secondPage.size()).isEqualTo(2);
    }
}
