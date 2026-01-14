package com.deokhugam.deokhugam.comment.integration.slice.jpa;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.comment.dto.request.CommentSearchCondition;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.repository.CommentQueryRepository;
import com.deokhugam.domain.comment.repository.CommentRepository;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.config.JpaAuditingConfig;
import com.deokhugam.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class, CommentQueryRepository.class})
public class CommentRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    EntityManagerFactory emf;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    CommentQueryRepository commentQueryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("DTO 프로젝션 성공")
    void findCommentDto() {
        //given
        User user = User.create("test@gmail", "test", "12345678q!");
        User savedUser = userRepository.save(user);
        Book book = Book.create(
                "title", "content", "12345678",
                LocalDate.now(), "publisher",
                "thumbnailUrl", "description");
        Book savedBook = bookRepository.save(book);
        Review review = Review.create(5.0, "content", savedBook, savedUser);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", savedUser, savedReview);
        Comment savedComment = commentRepository.save(comment);
        em.flush();
        em.clear();

        //when
        CommentDto commentDto = commentQueryRepository.findCommentDto(savedComment.getId()).orElseThrow();

        //then
        assertThat(commentDto.getContent()).isEqualTo(comment.getContent());
        assertThat(commentDto.getUserNickname()).isEqualTo(user.getNickname());
        assertThat(commentDto.getReviewId()).isEqualTo(savedReview.getId());
        assertThat(commentDto.getUserId()).isEqualTo(savedUser.getId());
    }

    @Nested
    @DisplayName("FetchJoin 메서드")
    class FetchJoin{
        @Test
        @DisplayName("Comment 조회 시 user를 페치 조인")
        void findWithUser() {
            //given
            User user = User.create("test@gmail", "test", "12345678q!");
            User savedUser = userRepository.save(user);
            Book book = Book.create(
                    "title", "content", "12345678",
                    LocalDate.now(), "publisher",
                    "thumbnailUrl", "description");
            Book savedBook = bookRepository.save(book);
            Review review = Review.create(5.0, "content", savedBook, savedUser);
            Review savedReview = reviewRepository.save(review);
            Comment comment = Comment.create("content", savedUser, savedReview);
            Comment savedComment = commentRepository.save(comment);
            em.flush();
            em.clear();

            //when
            Comment findComment = commentRepository.findWithUser(savedComment.getId()).orElseThrow();

            //then
            boolean userIsLoaded = emf.getPersistenceUnitUtil().isLoaded(findComment.getUser());
            assertThat(userIsLoaded).isTrue();
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
            assertThat(findComment.getUser().getNickname()).isEqualTo(user.getNickname());
        }

        @Test
        @DisplayName("Comment 조회 시 user와 review를 페치 조인")
        void findWithUserAndReview() {
            //given
            User user = User.create("test@gmail", "test", "12345678q!");
            User savedUser = userRepository.save(user);
            Book book = Book.create(
                    "title", "content", "12345678",
                    LocalDate.now(), "publisher",
                    "thumbnailUrl", "description");
            Book savedBook = bookRepository.save(book);
            Review review = Review.create(5.0, "content", savedBook, savedUser);
            Review savedReview = reviewRepository.save(review);
            Comment comment = Comment.create("content", savedUser, savedReview);
            Comment savedComment = commentRepository.save(comment);
            em.flush();
            em.clear();

            //when
            Comment findComment = commentRepository.findWithUserAndReview(savedComment.getId()).orElseThrow();

            //then
            boolean userIsLoaded = emf.getPersistenceUnitUtil().isLoaded(findComment.getUser());
            boolean reviewIsLoaded = emf.getPersistenceUnitUtil().isLoaded(findComment.getReview());
            assertThat(userIsLoaded).isTrue();
            assertThat(reviewIsLoaded).isTrue();
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
            assertThat(findComment.getUser().getNickname()).isEqualTo(user.getNickname());
            assertThat(findComment.getReview().getContent()).isEqualTo(review.getContent());
        }
    }


    @Nested
    @DisplayName("댓글 목록 커서 페이지네이션")
    class Search {
        @Test
        @DisplayName("댓글 목록 조회 성공 - 내림차순")
        void searchComments_no_cursor_desc() {
            //given
            User user = User.create("test@gmail", "test", "12345678q!");
            User savedUser = userRepository.save(user);
            Book book = Book.create(
                    "title", "content", "12345678",
                    LocalDate.now(), "publisher",
                    "thumbnailUrl", "description");
            Book savedBook = bookRepository.save(book);
            Review review = Review.create(5.0, "content", savedBook, savedUser);
            Review savedReview = reviewRepository.save(review);
            List<Comment> commentList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                Comment comment = Comment.create("content" + i, savedUser, savedReview);
                commentList.add(comment);
            }
            commentRepository.saveAll(commentList);
            em.flush();
            em.clear();

            CommentSearchCondition condition = new CommentSearchCondition(
                    savedReview.getId(),
                    "DESC",
                    null,
                    null,
                    20);

            //when
            List<Comment> results = commentRepository.searchComments(condition);


            //then
            assertThat(results.size()).isEqualTo(condition.limit());
            assertThat(results.get(0).getCreatedAt())
                    .isAfterOrEqualTo(results.get(results.size() - 1).getCreatedAt());
            assertThat(results).extracting(Comment::getCreatedAt).isSortedAccordingTo(Comparator.reverseOrder());
            assertThat(results).allSatisfy(comment -> {
                assertThat(comment.getReview().getId()).isEqualTo(savedReview.getId());
            });
        }

        @Test
        @DisplayName("댓글 목록 조회 성공- after값 기준 내림차순")
        void searchComments_after_desc() {
            //given
            User user = User.create("test@gmail", "test", "12345678q!");
            User savedUser = userRepository.save(user);
            Book book = Book.create(
                    "title", "content", "12345678",
                    LocalDate.now(), "publisher",
                    "thumbnailUrl", "description");
            Book savedBook = bookRepository.save(book);
            Review review = Review.create(5.0, "content", savedBook, savedUser);
            Review savedReview = reviewRepository.save(review);
            List<Comment> commentList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                Comment comment = Comment.create("content" + i, savedUser, savedReview);
                commentList.add(comment);
            }
            commentRepository.saveAll(commentList);
            em.flush();
            em.clear();

            CommentSearchCondition condition = new CommentSearchCondition(
                    savedReview.getId(),
                    "DESC",
                    commentList.get(75).getCreatedAt().toString(),
                    commentList.get(75).getCreatedAt(),
                    20);

            //when
            List<Comment> results = commentRepository.searchComments(condition);

            for (Comment result : results) {
                System.out.println("result = " + result.getContent());
                System.out.println("result = " + result.getCreatedAt());
            }

            //then
            assertThat(results.size()).isEqualTo(condition.limit());
            assertThat(results.get(0).getCreatedAt())
                    .isAfterOrEqualTo(results.get(results.size() - 1).getCreatedAt());
            assertThat(results).extracting(Comment::getCreatedAt).isSortedAccordingTo(Comparator.reverseOrder());
            assertThat(results).allSatisfy(comment -> {
                assertThat(comment.getReview().getId()).isEqualTo(savedReview.getId());
            });
            assertThat(results).allSatisfy(c ->
                    assertThat(c.getCreatedAt()).isBefore(commentList.get(75).getCreatedAt())
            );
        }
    }
}
