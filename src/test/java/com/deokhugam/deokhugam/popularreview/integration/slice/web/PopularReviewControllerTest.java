package com.deokhugam.deokhugam.popularreview.integration.slice.web;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.popularreview.controller.PopularReviewController;
import com.deokhugam.domain.popularreview.dto.request.PopularReviewSearchCondition;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewDto;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewPageResponseDto;
import com.deokhugam.domain.popularreview.service.PopularReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PopularReviewController.class)
public class PopularReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PopularReviewService popularReviewService;

    @Test
    @DisplayName("인기 리뷰 목록 조회 성공")
    void getPopularReview_Success() throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId =  UUID.randomUUID();

        List<PopularReviewDto> content = List.of(
                new PopularReviewDto(
                        reviewId, reviewId, bookId, "BookTitle",
                        "http//example.com/image.jpg", userId, "UserName",
                        "ReviewContent", 4.0, PeriodType.DAILY, Instant.now(), 1L,
                        90.0, 100L, 50L
                )
        );

        PopularReviewPageResponseDto response = new PopularReviewPageResponseDto(
                content, "nextCursorValue", Instant.now(), 1,  100L, true
        );

        Mockito.when(popularReviewService.getPopularReviews(any(PopularReviewSearchCondition.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reviews/popular")
                        .param("period", "DAILY")
                        .param("direction", "ASC")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(reviewId.toString()))
                .andExpect(jsonPath("$.content[0].bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.content[0].userNickname").value("UserName"))
                .andExpect(jsonPath("$.content[0].reviewContent").value("ReviewContent"))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    @DisplayName("[400] GET /api/reviews/popular - 잘못된 direction 파라미터로 인해 Bad Request 반환")
    void getPopularReviews_InvalidDirection_Returns400() throws Exception {
        mockMvc.perform(get("/api/reviews/popular")
                        .param("period", "DAILY")
                        .param("direction", "INVALID_DIRECTION")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }
}
