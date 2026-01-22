package com.deokhugam.deokhugam.likedreview.integration.slice.web;

import com.deokhugam.domain.likedreview.controller.LikedReviewController;
import com.deokhugam.domain.likedreview.dto.response.LikedReviewDto;
import com.deokhugam.domain.likedreview.service.LikedReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LikedReviewController.class)
class LikedReviewControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    LikedReviewService likedReviewService;

    @Test
    @DisplayName("좋아요 호출 성공")
    void likeReview_success() throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(likedReviewService.toggleLike(reviewId, userId))
                .thenReturn(new LikedReviewDto(reviewId, userId, true));

        mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId)
                        .header("Deokhugam-Request-User-ID", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    @DisplayName("좋아요 호출 시 헤더가 없으면 400 error")
    void likeReview_fail_error_400() throws Exception {
        UUID reviewId = UUID.randomUUID();

        mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId))
                .andExpect(status().isBadRequest());
    }
}

