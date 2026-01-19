package com.deokhugam.deokhugam.review.integration.slice.web;

import com.deokhugam.domain.review.controller.ReviewController;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;
import com.deokhugam.domain.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
public class ReviewControllerWebSliceTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReviewService reviewService;

    @Test
    @DisplayName("목록 조회 성공 시 200 반환")
    void list_success_return_200() throws Exception {
        // given
        UUID requestUserId = UUID.randomUUID();

        ReviewPageResponseDto response = new ReviewPageResponseDto(
                List.of(),
                null,
                null,
                0,
                0L,
                false
        );

        when(reviewService.searchReviews(any(), any(), any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/reviews")
                .header("Deokhugam-Request-User-ID", requestUserId.toString())
                .param("requestUserId", requestUserId.toString())
        )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상세 조회 성공 시 200 반환")
    void detail_success_return_200() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();
        Instant now = Instant.now();

        ReviewDto response = new ReviewDto(
                reviewId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "bookTitle",
                "bookThumbnailUrl",
                5.0,
                "nickname",
                "content",
                10L,
                5L,
                true,
                now,
                now
        );

        when(reviewService.getReview(requestUserId, reviewId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId)
                        .header("Deokhugam-Request-User-ID", requestUserId.toString())
                )
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("목록 조회 시 requestId와 headerId가 다르면 401에러 반환 후 서비스 호출 없음")
    void list_fail_idNotEqual_return_401() throws Exception {
        // given
        UUID requestUserId = UUID.randomUUID();
        UUID headerId = UUID.randomUUID();


        // when & then
        mockMvc.perform(get("/api/reviews")
                .header("Deokhugam-Request-User-ID", headerId.toString())
                .param("requestUserId", requestUserId.toString())
        )
                .andExpect(status().isUnauthorized());

        verify(reviewService, never()).searchReviews(any(), any(), any());
    }


}
