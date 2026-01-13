package com.deokhugam.deokhugam.review.integration.slice.web;

import com.deokhugam.domain.review.controller.ReviewController;
import com.deokhugam.domain.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(ReviewController.class)
class ReviewControllerBindingTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReviewService reviewService;

    @Test
    @DisplayName("requestUserId와 headerId가 다르면 에러가 발생한다")
    void list_headerUuidInvalid_return_error() throws Exception {
        mockMvc.perform(get("/api/reviews")
                .header("Deokhugam-Request-User-ID", "invalid-uuid")
                .param("requestUserId", UUID.randomUUID().toString())
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("path값이 잘못되면 에러가 발생한다")
    void get_pathUuidInvalid_return_error() throws Exception {
        mockMvc.perform(get("/api/reviews/invalid-uuid")
                .header("Deokhugam-Request-User-ID", UUID.randomUUID().toString())
        )
                .andExpect(status().isBadRequest());
    }

}
