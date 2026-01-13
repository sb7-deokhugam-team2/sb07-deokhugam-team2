package com.deokhugam.deokhugam.comment.unit.service;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.exception.CommentNotFound;
import com.deokhugam.domain.comment.exception.CommentUnauthorizedException;
import com.deokhugam.domain.comment.repository.CommentRepository;
import com.deokhugam.domain.comment.service.CommentServiceImpl;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.user.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl 테스트")
public class CommentServiceImplTest {

    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    CommentServiceImpl commentService;

//    @Test
//    @DisplayName("댓글 목록 조회 성공")
//    void findComments(){
//        //given
//
//        //when
//
//
//        //then
//
//    }
//
//    @Test
//    @DisplayName("댓글 생성 성공")
//    void createComment(){
//        //given
//
//        //when
//
//
//        //then
//
//    }

    @Test
    @DisplayName("댓글 조회 성공")
    void findComment(){
        //given
        User user = User.create("test@gmail", "test", "12345678q!");
        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        Comment comment = Comment.create("content", user, review);
        CommentDto from = CommentDto.from(comment);
        when(commentRepository.findCommentDto(any(UUID.class)))
                .thenReturn(Optional.of(from));

        //when
        commentService.findComment(UUID.randomUUID());

        //then
        verify(commentRepository).findCommentDto(any(UUID.class));
    }

    @Test
    @DisplayName("댓글 조회 실패 - 댓글 없음")
    void findComment_not_found(){
        //given
        when(commentRepository.findCommentDto(any(UUID.class)))
                .thenReturn(Optional.empty());

        //when&then
        assertThatThrownBy(()->commentService.findComment(UUID.randomUUID()))
                .isInstanceOf(CommentNotFound.class);
        verify(commentRepository).findCommentDto(any(UUID.class));
    }

    @Test
    @DisplayName("댓글 논리 삭제 성공")
    void logicalComment(){
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
    void logicalComment_unauthorized(){
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
        assertThatThrownBy(()->commentService.logicalDelete(UUID.randomUUID(), userId))
                .isInstanceOf(CommentUnauthorizedException.class);

        verify(commentRepository).findWithUser(any(UUID.class));
        assertThat(comment.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("댓글 물리 삭제 성공")
    void physicalComment(){
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
    void physicalComment_unauthorized(){
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
        assertThatThrownBy(()->commentService.physicalDelete(UUID.randomUUID(), userId))
                .isInstanceOf(CommentUnauthorizedException.class);

        verify(commentRepository).findWithUser(any(UUID.class));
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment(){
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
    void updateComment_unauthorized(){
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
        assertThatThrownBy(()->commentService.updateComment(UUID.randomUUID(), userId, commentUpdateRequest))
                .isInstanceOf(CommentUnauthorizedException.class);

        verify(commentRepository).findWithUserAndReview(any(UUID.class));
        assertThat(comment.getContent()).isEqualTo("content");
    }
}
