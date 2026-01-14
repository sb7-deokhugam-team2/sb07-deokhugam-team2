package com.deokhugam.deokhugam.comment.integration.slice.web;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.comment.controller.CommentController;
import com.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.exception.CommentNotFound;
import com.deokhugam.domain.comment.service.CommentService;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import(GlobalExceptionHandler.class)
public class CommentControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    CommentService commentService;

    @Test
    @DisplayName("댓글 조회 성공")
    void getComment() throws Exception {
        //given
        User user = User.create("test@gmail", "test", "12345678q!");
        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        Comment comment = Comment.create("content", user, review);
        CommentDto from = CommentDto.from(comment);
        when(commentService.findComment(any(UUID.class)))
                .thenReturn(from);
        UUID commentId = UUID.randomUUID();

        //when&then
        mockMvc.perform(get("/api/comments/{commentId}", commentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userNickname").value(user.getNickname()))
                .andExpect(jsonPath("$.content").value(comment.getContent()));

        verify(commentService).findComment(any(UUID.class));
    }

    @Test
    @DisplayName("댓글 조회 실패 - CommentNotFoundException 발생")
    void getComment_no_comment() throws Exception {
        //given
        User user = User.create("test@gmail", "test", "12345678q!");
        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        Comment comment = Comment.create("content", user, review);
        CommentDto from = CommentDto.from(comment);
        when(commentService.findComment(any(UUID.class)))
                .thenThrow(new CommentNotFound(ErrorCode.COMMENT_NOT_FOUND));
        UUID commentId = UUID.randomUUID();

        //when&then
        mockMvc.perform(get("/api/comments/{commentId}", commentId))
                .andDo(print())
                .andExpect(status().isNotFound());
        verify(commentService).findComment(any(UUID.class));
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment() throws Exception {
        //given
        User user = User.create("test@gmail", "test", "12345678q!");
        Book book = Book.create("title", "content", "12345678", LocalDate.now(), "publisher", "thumbnailUrl", "description");
        Review review = Review.create(5.0, "content", book, user);
        Comment comment = Comment.create("newContent", user, review);
        CommentDto from = CommentDto.from(comment);
        when(commentService.updateComment(any(UUID.class), any(UUID.class), any(CommentUpdateRequest.class)))
                .thenReturn(from);
        UUID commentId = UUID.randomUUID();
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("newContent");

        //when&then
        mockMvc.perform(patch("/api/comments/{commentId}", commentId)
                        .header("Deokhugam-Request-Id", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userNickname").value(user.getNickname()));

        verify(commentService).updateComment(any(UUID.class), any(UUID.class), any(CommentUpdateRequest.class));
    }

    @Test
    @DisplayName("댓글 수정 실패 - content값이 넘어오지 않음")
    void updateComment_no_content() throws Exception {

        //when&then
        mockMvc.perform(patch("/api/comments/{commentId}", UUID.randomUUID())
                        .header("Deokhugam-Request-Id", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(commentService, never()).updateComment(any(UUID.class), any(UUID.class), any(CommentUpdateRequest.class));
    }

    @Test
    @DisplayName("댓글 수정 실패 - header값이 넘어오지 않음")
    void updateComment_no_user_id() throws Exception {
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("newContent");
        //when&then
        mockMvc.perform(patch("/api/comments/{commentId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(commentService, never()).updateComment(any(UUID.class), any(UUID.class), any(CommentUpdateRequest.class));
    }

    @Test
    @DisplayName("댓글 수정 실패 - contentType 누락")
    void updateComment_no_content_type() throws Exception {
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("newContent");
        //when&then
        mockMvc.perform(patch("/api/comments/{commentId}", UUID.randomUUID())
                        .header("Deokhugam-Request-Id", UUID.randomUUID())
                        .content(objectMapper.writeValueAsString(commentUpdateRequest)))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());

        verify(commentService, never()).updateComment(any(UUID.class), any(UUID.class), any(CommentUpdateRequest.class));
    }

    @Test
    @DisplayName("댓글 논리 삭제 성공")
    void logicalDelete() throws Exception {
        //when&then
        mockMvc.perform(delete("/api/comments/{commentId}", UUID.randomUUID())
                        .header("Deokhugam-Request-Id", UUID.randomUUID()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(commentService).logicalDelete(any(UUID.class), any(UUID.class));
    }

    @Test
    @DisplayName("댓글 물리 삭제 성공")
    void physicalDelete() throws Exception {
        //when&then
        mockMvc.perform(delete("/api/comments/{commentId}/hard", UUID.randomUUID())
                        .header("Deokhugam-Request-Id", UUID.randomUUID()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(commentService).physicalDelete(any(UUID.class), any(UUID.class));
    }
}
