package com.deokhugam.deokhugam.review.integration.slice.web;

import com.deokhugam.domain.review.controller.ReviewController;
import com.deokhugam.domain.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerValidationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ReviewService reviewService;

    @Test
    @DisplayName("리뷰 생성 요청 시 잘못된 content 값이 오면 에러가 발생한다")
    void create_contentBlank_return_error() throws Exception {
        Map<String, Object> body = Map.of(
                "bookId", UUID.randomUUID(),
                "userId", UUID.randomUUID(),
                "content", "    ",
                "rating", 4.5
        );

        mockMvc.perform(post("/api/reviews")
                        .header("Deokhugam-Request-User-ID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰 생성 요청 시 잘못된 평점이 오면 에러가 발생한다")
    void create_ratingOutOfRange_return_error() throws Exception {
        Map<String, Object> body = Map.of(
                "bookId", UUID.randomUUID(),
                "userId", UUID.randomUUID(),
                "content", "content",
                "rating", 5.1
        );

        mockMvc.perform(post("/api/reviews")
                .header("Deokhugam-Request-User-ID", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        )
                .andExpect(status().isBadRequest());
    }
}
