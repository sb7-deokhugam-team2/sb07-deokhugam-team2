package com.deokhugam.deokhugam.comment.unit.service;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.domain.comment.dto.request.CommentSearchCondition;
import com.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.dto.response.CursorPageResponseCommentDto;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.exception.CommentNotFound;
import com.deokhugam.domain.comment.exception.CommentUnauthorizedException;
import com.deokhugam.domain.comment.repository.CommentQueryRepository;
import com.deokhugam.domain.comment.repository.CommentRepository;
import com.deokhugam.domain.comment.service.CommentServiceImpl;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.exception.UserNotFoundException;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.global.exception.GlobalExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl 테스트")
public class CommentServiceImplTest {

    @Mock
    CommentRepository commentRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ReviewRepository reviewRepository;
    @Mock
    CommentQueryRepository commentQueryRepository;

    @InjectMocks
    CommentServiceImpl commentService;

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void findComments_cursor_success(){
        //given
        User user = User.create("test@gmail.com"," test123", "12345678a!");
        Book book = Book.create("title","author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        UUID reviewId = UUID.randomUUID();

        //첫 페이지 20개 조회, 전체 갯수는 100개
        String direction = "DESC";
        String cursor = null;
        Instant after = null;
        Integer limit = 20;

        List<Comment> result = new ArrayList<>();
        Instant time = Instant.now();
        Instant lastTime = Instant.now().minus(2, ChronoUnit.DAYS);
        for(int i = 21; i>0; i--){
            Comment comment = Comment.create("content" + i, user, review);
            if(i==2){
                ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
                ReflectionTestUtils.setField(comment, "createdAt", lastTime);
                ReflectionTestUtils.setField(comment, "updatedAt", lastTime);
            } else {
                ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
                ReflectionTestUtils.setField(comment, "createdAt", time);
                ReflectionTestUtils.setField(comment, "updatedAt", time);
            }
            result.add(comment);
        }
        CommentSearchCondition searchCondition = new CommentSearchCondition(reviewId, direction, cursor, after, limit);
        when(commentRepository.searchComments(searchCondition)).thenReturn(result);
        when(commentRepository.count()).thenReturn(100L);

        //when
        CursorPageResponseCommentDto cursorPageResponseCommentDto = commentService.findContents(searchCondition);

        //then
        assertThat(cursorPageResponseCommentDto.getContent().size()).isEqualTo(limit);
        assertThat(cursorPageResponseCommentDto.isHasNext()).isTrue();
        assertThat(cursorPageResponseCommentDto.getTotalElements()).isEqualTo(100L);
        assertThat(cursorPageResponseCommentDto.getNextCursor()).isEqualTo(lastTime.toString());
        assertThat(cursorPageResponseCommentDto.getNextAfter()).isEqualTo(lastTime);
    }

    @Test
    @DisplayName("댓글 생성 성공")
    void createComment_success() {
        //given
        User user = User.create("test@gmail.com", "test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(5.0, "content", book, user);
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(reviewId, userId, "와!");

        when(userRepository.findById(commentCreateRequest.userId())).thenReturn(Optional.of(user));
        when(reviewRepository.findById(commentCreateRequest.reviewId())).thenReturn(Optional.of(review));

        //when
        CommentDto commentDto = commentService.createComment(commentCreateRequest);

        //then
        assertThat(commentDto).isNotNull();
    }

    @Test
    @DisplayName("댓글 생성 실패 : 1. 유저를 찾지 못함")
    void createComment_fail_user() {
        //given
        User user = User.create("test@gmail.com", "test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(5.0, "content", book, user);
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(reviewId, userId, "와!");

        when(userRepository.findById(commentCreateRequest.userId())).thenReturn(Optional.empty());

        //when&then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            commentService.createComment(commentCreateRequest);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 생성 실패 : 2. 리뷰 정보를 찾지 못함")
    void createComment_fail_reviewId() {
        //given
        User user = User.create("test@gmail.com", "test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(5.0, "content", book, user);
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(reviewId, userId, "와!");

        when(userRepository.findById(commentCreateRequest.userId())).thenReturn(Optional.of(user));
        when(reviewRepository.findById(commentCreateRequest.reviewId())).thenReturn(Optional.empty());

        //when&then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            commentService.createComment(commentCreateRequest);
        });

        assertThat(exception.getMessage()).isEqualTo("요청한 리뷰 정보를 찾을 수 없습니다.");
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 조회 성공")
    void findComment() {
        //given
        User user = User.create("test@gmail", "test", "12345678q!");
        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        Comment comment = Comment.create("content", user, review);
        CommentDto from = CommentDto.from(comment);
        when(commentQueryRepository.findCommentDto(any(UUID.class)))
                .thenReturn(Optional.of(from));

        //when
        commentService.findComment(UUID.randomUUID());

        //then
        verify(commentQueryRepository).findCommentDto(any(UUID.class));
    }

    @Test
    @DisplayName("댓글 조회 실패 - 댓글 없음")
    void findComment_not_found() {
        //given
        when(commentQueryRepository.findCommentDto(any(UUID.class)))
                .thenReturn(Optional.empty());

        //when&then
        assertThatThrownBy(() -> commentService.findComment(UUID.randomUUID()))
                .isInstanceOf(CommentNotFound.class);
        verify(commentQueryRepository).findCommentDto(any(UUID.class));
    }

    @Test
    @DisplayName("댓글 논리 삭제 성공")
    void logicalComment() {
        //given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@gmail", "test", "12345678q!");

        ReflectionTestUtils.setField(user, "id", userId);

        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        Comment comment = Comment.create("content", user, review);
        when(commentRepository.findWithUser(any(UUID.class)))
                .thenReturn(Optional.of(comment));

        //when
        commentService.logicalDelete(UUID.randomUUID(), userId);

        //then
        verify(commentRepository).findWithUser(any(UUID.class));
        assertThat(comment.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("댓글 논리 삭제 실패 - 유저 권한 없음")
    void logicalComment_unauthorized() {
        //given
        UUID userId = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        User user = User.create("test@gmail", "test", "12345678q!");

        ReflectionTestUtils.setField(user, "id", userId2);

        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        Comment comment = Comment.create("content", user, review);
        when(commentRepository.findWithUser(any(UUID.class)))
                .thenReturn(Optional.of(comment));

        //when&then
        assertThatThrownBy(() -> commentService.logicalDelete(UUID.randomUUID(), userId))
                .isInstanceOf(CommentUnauthorizedException.class);

        verify(commentRepository).findWithUser(any(UUID.class));
        assertThat(comment.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("댓글 물리 삭제 성공")
    void physicalComment() {
        //given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@gmail", "test", "12345678q!");

        ReflectionTestUtils.setField(user, "id", userId);

        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        Comment comment = Comment.create("content", user, review);
        when(commentRepository.findWithUser(any(UUID.class)))
                .thenReturn(Optional.of(comment));

        //when
        commentService.physicalDelete(UUID.randomUUID(), userId);

        //then
        verify(commentRepository).findWithUser(any(UUID.class));
        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("댓글 물리 삭제 실패 - 유저 권한 없음")
    void physicalComment_unauthorized() {
        //given
        UUID userId = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        User user = User.create("test@gmail", "test", "12345678q!");

        ReflectionTestUtils.setField(user, "id", userId2);

        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        Comment comment = Comment.create("content", user, review);
        when(commentRepository.findWithUser(any(UUID.class)))
                .thenReturn(Optional.of(comment));

        //when&then
        assertThatThrownBy(() -> commentService.physicalDelete(UUID.randomUUID(), userId))
                .isInstanceOf(CommentUnauthorizedException.class);

        verify(commentRepository).findWithUser(any(UUID.class));
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment() {
        //given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@gmail", "test", "12345678q!");
        ReflectionTestUtils.setField(user, "id", userId);
        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        Comment comment = Comment.create("content", user, review);
        when(commentRepository.findWithUserAndReview(any(UUID.class)))
                .thenReturn(Optional.of(comment));
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("updateContent");

        //when
        CommentDto commentDto = commentService.updateComment(UUID.randomUUID(), userId, commentUpdateRequest);

        //then
        verify(commentRepository).findWithUserAndReview(any(UUID.class));
        assertThat(commentDto.getContent()).isEqualTo(commentUpdateRequest.content());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 유저 권한 없음")
    void updateComment_unauthorized() {
        //given
        UUID userId = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        User user = User.create("test@gmail", "test", "12345678q!");

        ReflectionTestUtils.setField(user, "id", userId2);

        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        Comment comment = Comment.create("content", user, review);
        when(commentRepository.findWithUserAndReview(any(UUID.class)))
                .thenReturn(Optional.of(comment));
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("updateContent");

        //when&then
        assertThatThrownBy(() -> commentService.updateComment(UUID.randomUUID(), userId, commentUpdateRequest))
                .isInstanceOf(CommentUnauthorizedException.class);

        verify(commentRepository).findWithUserAndReview(any(UUID.class));
        assertThat(comment.getContent()).isEqualTo("content");
    }
}
