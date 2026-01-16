package com.deokhugam.deokhugam.comment.integration.slice.web;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.comment.controller.CommentController;
import com.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.domain.comment.dto.request.CommentSearchCondition;
import com.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.dto.response.CursorPageResponseCommentDto;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.exception.CommentNotFound;
import com.deokhugam.domain.comment.service.CommentService;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
    @DisplayName("댓글 목록 조회 성공")
    void getComments_success() throws Exception {
    // given
    User user = User.create("test@gmail.com", "test123", "12345678a!");
    Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "","description");
    Review review = Review.create(4.0, "content", book, user);
    UUID reviewId = UUID.randomUUID();

    List<CommentDto> result = new ArrayList<>();
        Comment comment = Comment.create("content", user, review);
        CommentDto commentDto = CommentDto.from(comment);
        result.add(commentDto);

    CursorPageResponseCommentDto cursorPageResponseCommentDto
            = new CursorPageResponseCommentDto(
            result, Instant.now().toString(),Instant.now(),50,100L, false
    );
    when(commentService.findContents(any(CommentSearchCondition.class))).thenReturn(cursorPageResponseCommentDto);

    // when&then
    mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString())
            .param("direction", "DESC")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(50))
        .andExpect(jsonPath("$.totalElements").value(100))
        .andExpect(jsonPath("$.hasNext").value(false));

    verify(commentService, times(1)).findContents(any(CommentSearchCondition.class));
    }

    @Test
    @DisplayName("댓글 작성 성공")
    void createComment_success() throws Exception {
    // given
    User user = User.create("test@gmail.com", "test123", "12345678a!");
    Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "","description");
    Review review = Review.create(4.0, "content", book, user);
    Comment comment = Comment.create("content", user, review);
    CommentDto commentDto = CommentDto.from(comment);
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    CommentCreateRequest commentCreateRequest = new CommentCreateRequest(reviewId, userId, "content");

    when(commentService.createComment(commentCreateRequest)).thenReturn(commentDto);

    // when&then
    mockMvc.perform(post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(commentCreateRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("content"))
            .andExpect(jsonPath("$.userNickname").value("test123"))
            .andDo(print());

    verify(commentService, times(1)).createComment(commentCreateRequest);
    }

    @Test
    @DisplayName("댓글 작성 실패 : 1. reviewId가 없음")
    void createComment_fail_reviewId() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(null, userId, "content");

        // when&then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(commentService, never()).createComment(commentCreateRequest);
    }

    @Test
    @DisplayName("댓글 작성 실패 : 2. userId가 없음")
    void createComment_fail_userId() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(reviewId, null, "content");

        // when&then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(commentService, never()).createComment(commentCreateRequest);
    }

    @Test
    @DisplayName("댓글 작성 실패 : 3. content가 없음")
    void createComment_fail_content() throws Exception {
        // given
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(UUID.randomUUID(), UUID.randomUUID(), null);

        // when&then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(commentService, never()).createComment(commentCreateRequest);
    }

    @Test
    @DisplayName("댓글 작성 @NotBlank 확인 : 1. 빈 문자열은 입력될 수 없음")
    void createComment_no_text() throws Exception {
        // given
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(UUID.randomUUID(), UUID.randomUUID(), "");

        // when&then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(commentService, never()).createComment(commentCreateRequest);
    }

    @Test
    @DisplayName("댓글 작성 @NotBlank 확인 : 2. 공백은 입력될 수 없음")
    void createComment_no_blank() throws Exception {
        // given
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(UUID.randomUUID(), UUID.randomUUID(), "  ");

        // when&then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(commentService, never()).createComment(commentCreateRequest);
    }

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
                        .header("Deokhugam-Request-User-Id", UUID.randomUUID())
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
                        .header("Deokhugam-Request-User-Id", UUID.randomUUID())
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
                        .header("Deokhugam-Request-User-Id", UUID.randomUUID()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(commentService).logicalDelete(any(UUID.class), any(UUID.class));
    }

    @Test
    @DisplayName("댓글 논리 삭제 실패")
    void logicalDelete_no_header() throws Exception {
        //when&then
        mockMvc.perform(delete("/api/comments/{commentId}", UUID.randomUUID()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(commentService, never()).logicalDelete(any(UUID.class), any(UUID.class));
    }

    @Test
    @DisplayName("댓글 물리 삭제 성공")
    void physicalDelete() throws Exception {
        //when&then
        mockMvc.perform(delete("/api/comments/{commentId}/hard", UUID.randomUUID())
                        .header("Deokhugam-Request-User-Id", UUID.randomUUID()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(commentService).physicalDelete(any(UUID.class), any(UUID.class));
    }

    @Test
    @DisplayName("댓글 물리 삭제 실패")
    void physicalDelete_no_header() throws Exception {
        //when&then
        mockMvc.perform(delete("/api/comments/{commentId}", UUID.randomUUID()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(commentService, never()).logicalDelete(any(UUID.class), any(UUID.class));
    }
}
