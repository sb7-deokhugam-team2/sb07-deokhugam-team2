package com.deokhugam.deokhugam.comment.integration.service;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.domain.comment.dto.request.CommentSearchCondition;
import com.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.dto.response.CursorPageResponseCommentDto;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.exception.CommentNotFound;
import com.deokhugam.domain.comment.exception.CommentUnauthorizedException;
import com.deokhugam.domain.comment.repository.CommentRepository;
import com.deokhugam.domain.comment.service.CommentService;
import com.deokhugam.domain.notification.service.NotificationCreator;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.exception.UserNotFoundException;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@DisplayName("CommentServiceIntegraionTest")
public class CommentServiceIntegrationTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    CommentService commentService;
    @Autowired
    EntityManager em;
    @Autowired
    CommentRepository commentRepository;
    @MockitoBean
    NotificationCreator notificationCreator;

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void findComments_cursor_success() {
        //given
        User user = User.create("test12@gmail.com", " test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        userRepository.save(user);
        bookRepository.save(book);
        reviewRepository.save(review);


        //첫 페이지 20개 조회, 전체 갯수는 100개
        String direction = "DESC";
        String cursor = null;
        Instant after = null;
        Integer limit = 20;

        UUID cursorCommentId = null;
        for (int i = 21; i > 0; i--) {
            Comment comment = Comment.create("content" + i, user, review);
            commentRepository.save(comment);
            if (i == 20) {
                cursorCommentId = comment.getId();
            }
        }
        em.flush();
        em.clear();
        Comment comment = commentRepository.findById(cursorCommentId).orElseThrow();

        CommentSearchCondition searchCondition = new CommentSearchCondition(
                review.getId(),
                direction,
                cursor,
                after,
                limit);


        //when
        CursorPageResponseCommentDto cursorPageResponseCommentDto = commentService.findContents(searchCondition);

        //then
        assertThat(cursorPageResponseCommentDto.getContent().size()).isEqualTo(limit);
        assertThat(cursorPageResponseCommentDto.isHasNext()).isTrue();
        assertThat(cursorPageResponseCommentDto.getNextCursor()).isEqualTo(comment.getCreatedAt().toString());
        assertThat(cursorPageResponseCommentDto.getNextAfter()).isEqualTo(comment.getCreatedAt());

    }

    @Test
    @DisplayName("댓글 생성 성공")
    void createComment_success() {
        //given
        User user = User.create("test12@gmail.com", " test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        User savedUser = userRepository.save(user);
        bookRepository.save(book);
        Review savedReview = reviewRepository.save(review);

        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(savedReview.getId(), savedUser.getId(), "와!");


        //when
        CommentDto commentDto = commentService.createComment(commentCreateRequest);

        verify(notificationCreator, times(1)).createNotification(any(Comment.class));

        //then
        assertThat(commentDto).isNotNull();
        assertThat(commentDto.getContent()).isEqualTo(commentCreateRequest.content());
        assertThat(commentDto.getUserId()).isEqualTo(savedUser.getId());
        assertThat(commentDto.getReviewId()).isEqualTo(savedReview.getId());
    }

    @Test
    @DisplayName("댓글 생성 실패 : 1. 유저를 찾지 못함")
    void createComment_fail_user() {
        //given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(reviewId, userId, "와!");


        //when&then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            commentService.createComment(commentCreateRequest);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 생성 실패 : 2. 리뷰 정보를 찾지 못함")
    void createComment_fail_reviewId() {
        //given
        User user = User.create("test12@gmail.com", " test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");

        User savedUser = userRepository.save(user);
        bookRepository.save(book);


        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(UUID.randomUUID(), savedUser.getId(), "와!");

        //when&then
        ReviewNotFoundException exception = assertThrows(ReviewNotFoundException.class, () -> {
            commentService.createComment(commentCreateRequest);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 조회 성공")
    void findComment() {
        //given
        User user = User.create("test12@gmail.com", " test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        User savedUser = userRepository.save(user);
        bookRepository.save(book);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", user, review);
        Comment savedComment = commentRepository.save(comment);

        //when
        CommentDto commentDto = commentService.findComment(savedComment.getId());

        //then
        assertThat(commentDto.getId()).isEqualTo(savedComment.getId());
        assertThat(commentDto.getContent()).isEqualTo(savedComment.getContent());
    }

    @Test
    @DisplayName("댓글 조회 실패 - 댓글 없음")
    void findComment_not_found() {
        //when&then
        assertThatThrownBy(() -> commentService.findComment(UUID.randomUUID()))
                .isInstanceOf(CommentNotFound.class);
    }

    @Test
    @DisplayName("댓글 논리 삭제 성공")
    void logicalComment() {
        //given
        User user = User.create("test12@gmail.com", " test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        User savedUser = userRepository.save(user);
        bookRepository.save(book);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", user, review);
        Comment savedComment = commentRepository.save(comment);

        //when
        commentService.logicalDelete(savedComment.getId(), savedUser.getId());

        //then
        assertThat(savedComment.isDeleted()).isTrue();
        assertThatThrownBy(() -> commentService.findComment(savedComment.getId()))
                .isInstanceOf(CommentNotFound.class);
    }

    @Test
    @DisplayName("댓글 논리 삭제 실패 - 유저 권한 없음")
    void logicalComment_unauthorized() {
        //given
        User user = User.create("test12@gmail.com", " test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        User savedUser = userRepository.save(user);
        bookRepository.save(book);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", user, review);
        Comment savedComment = commentRepository.save(comment);

        //when&then
        assertThatThrownBy(() -> commentService.logicalDelete(savedComment.getId(), UUID.randomUUID()))
                .isInstanceOf(CommentUnauthorizedException.class);

        assertThat(savedComment.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("댓글 물리 삭제 성공")
    void physicalComment() {
        //given
        User user = User.create("test12@gmail.com", " test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        User savedUser = userRepository.save(user);
        bookRepository.save(book);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", user, review);
        Comment savedComment = commentRepository.save(comment);

        //when
        commentService.physicalDelete(savedComment.getId(), savedUser.getId());

        //then
        assertThatThrownBy(() -> commentService.findComment(savedComment.getId()))
                .isInstanceOf(CommentNotFound.class);
    }

    @Test
    @DisplayName("댓글 물리 삭제 실패 - 유저 권한 없음")
    void physicalComment_unauthorized() {
        //given
        User user = User.create("test12@gmail.com", " test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        User savedUser = userRepository.save(user);
        bookRepository.save(book);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", user, review);
        Comment savedComment = commentRepository.save(comment);


        //when&then
        assertThatThrownBy(() -> commentService.physicalDelete(savedComment.getId(), UUID.randomUUID()))
                .isInstanceOf(CommentUnauthorizedException.class);
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment() {
        //given
        User user = User.create("test12@gmail.com", " test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        User savedUser = userRepository.save(user);
        bookRepository.save(book);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", user, review);
        Comment savedComment = commentRepository.save(comment);

        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("updateContent");

        //when
        CommentDto commentDto = commentService.updateComment(savedComment.getId(), savedUser.getId(), commentUpdateRequest);

        //then
        assertThat(commentDto.getContent()).isEqualTo(commentUpdateRequest.content());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 유저 권한 없음")
    void updateComment_unauthorized() {
        //given
        User user = User.create("test12@gmail.com", " test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        User savedUser = userRepository.save(user);
        bookRepository.save(book);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", user, review);
        Comment savedComment = commentRepository.save(comment);
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("updateContent");

        //when&then
        assertThatThrownBy(() -> commentService.updateComment(savedComment.getId(), UUID.randomUUID(), commentUpdateRequest))
                .isInstanceOf(CommentUnauthorizedException.class);

        assertThat(comment.getContent()).isEqualTo("content");
    }
}
