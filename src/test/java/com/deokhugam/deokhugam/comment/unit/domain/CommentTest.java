package com.deokhugam.deokhugam.comment.unit.domain;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.exception.CommentContentException;
import com.deokhugam.domain.comment.exception.CommentReviewNullException;
import com.deokhugam.domain.comment.exception.CommentUserNullException;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Comment Test")
public class CommentTest {


    @Test
    @DisplayName("create 성공")
    void create() {
        User user = User.create("test@gmail", "test", "12345678q!");
        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);

        Comment comment = Comment.create("content", user, review);
    }

    @Test
    @DisplayName("create 실패 - content가 공백일 경우 validate 실패")
    void create_validate_content() {
        User user = User.create("test@gmail", "test", "12345678q!");
        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        assertThatThrownBy(() -> Comment.create(" ", user, review))
                .isInstanceOf(CommentContentException.class);
    }

    @Test
    @DisplayName("create 실패 - user가 null일 경우 validate 실패")
    void create_validate_user() {
        User user = null;
        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        assertThatThrownBy(() -> Comment.create(" ", user, review))
                .isInstanceOf(CommentUserNullException.class);
    }

    @Test
    @DisplayName("create 실패 -review가 null일 경우 validate 실패")
    void create_validate_review() {
        User user = User.create("test@gmail", "test", "12345678q!");
        Review review = null;
        assertThatThrownBy(() -> Comment.create(" ", user, review))
                .isInstanceOf(CommentReviewNullException.class);
    }

    @Test
    @DisplayName("isAuthor 성공")
    void isAuthor() {
        UUID userId = UUID.randomUUID();
        User user = User.create("test@gmail", "test", "12345678q!");
        ReflectionTestUtils.setField(user, "id", userId);
        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        Comment comment = Comment.create("content", user, review);

        boolean result = comment.isAuthor(userId);

        assertThat(result).isTrue();
    }
}
