package com.deokhugam.deokhugam.review.integration.slice.web;

import com.deokhugam.domain.review.controller.ReviewController;
import com.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.deokhugam.domain.review.service.ReviewServiceImpl;
import com.deokhugam.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    ReviewServiceImpl reviewService;

    UUID bookId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    @Test
    @DisplayName("리뷰 생성 테스트 - 성공")
    void createReview_Success() throws Exception {
        // given
        ReviewCreateRequest request = new ReviewCreateRequest(
                bookId,
                userId,
                5.0,
                "리뷰 내용"
        );

        // when & then
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(reviewService, times(1)).createReview(any(ReviewCreateRequest.class));
    }

    @Test
    @DisplayName("리뷰 생성 테스트 - 실패 (필수 데이터 누락)")
    void createReview_Failure() throws Exception {
        // given
        ReviewCreateRequest request = new ReviewCreateRequest(
                null,
                userId,
                5.0,
                "리뷰 내용"
        );

        // when & then
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰 수정 테스트 - 성공")
    void updateReview_Success() throws Exception {
        // given
        ReviewUpdateRequest request = new ReviewUpdateRequest(
                4.0,
                "수정된 리뷰 내용"
        );

        // when & then
        mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
                .header("Deokhugam-Request-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(reviewService, times(1))
                .updateReview(any(ReviewUpdateRequest.class),eq(reviewId), eq(userId));
    }

    @Test
    @DisplayName("리뷰 수정 테스트 - 실패 (헤더 누락)")
    void updateReview_Failure() throws Exception {
        // given
        ReviewUpdateRequest request = new ReviewUpdateRequest(
                4.0,
                "수정된 리뷰 내용"
        );

        // when & then
        mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
                        // 헤더를 누락 또는 빈 값으로 설정
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰 논리 삭제  테스트 - 성공")
    void softDeleteReview_Success() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                    .header("Deokhugam-Request-User-ID", userId.toString()))
                .andExpect(status().isNoContent());

        verify(reviewService, times(1)).softDeleteReview(reviewId, userId);
    }

    @Test
    @DisplayName("리뷰 논리 삭제 테스트 - 실패 (리소스 미존재)")
    void softDeleteReview_Failure() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND)).when(reviewService).softDeleteReview(any(), any());

        // when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                        .header("Deokhugam-Request-User-ID", userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("리뷰 물리 삭제 테스트 - 성공")
    void hardDeleteReview_Success() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}/hard", reviewId)
                        .header("Deokhugam-Request-User-ID", userId.toString()))
                .andExpect(status().isNoContent());

        verify(reviewService, times(1)).hardDeleteReview(reviewId, userId);

    }

    @Test
    @DisplayName("리뷰 물리 삭제 테스트 - 실패 (유효하지 않은 헤더)")
    void hardDeleteReview_Failure() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        String invalidUserId = "invalid-uuid";

        // when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}/hard", reviewId)
                        .header("Deokhugam-Request-User-ID", invalidUserId))
                .andExpect(status().isBadRequest());
    }
}
