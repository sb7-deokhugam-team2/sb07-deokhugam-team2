package com.deokhugam.deokhugam.popularbook.integration.slice.web;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.enums.SortDirection;
import com.deokhugam.domain.popularbook.controller.PopularBookController;
import com.deokhugam.domain.popularbook.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.popularbook.dto.response.CursorPageResponsePopularBookDto;
import com.deokhugam.domain.popularbook.dto.response.PopularBookDto;
import com.deokhugam.domain.popularbook.service.PopularBookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PopularBookController.class)
public class PopularBookControllerSliceTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PopularBookService popularBookService;

    @Nested
    @DisplayName("getPopularBooks")
    class GetPopularBooks {

        @Test
        @DisplayName("[200] GET /api/books/popular - 기본 요청은 200 OK와 응답 바디를 반환한다")
        void getPopularBooks_returns200() throws Exception {
            // given
            CursorPageResponsePopularBookDto response = new CursorPageResponsePopularBookDto(
                    List.of(sampleDto(1L)),
                    null,
                    null,
                    1,
                    1L,
                    false
            );
            given(popularBookService.searchPopularBooks(any(PopularBookSearchCondition.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/books/popular")
                            .param("period", "DAILY")
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("application/json"))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.hasNext").value(false))
                    .andExpect(jsonPath("$.totalElements").value(1));


            then(popularBookService).should().searchPopularBooks(any(PopularBookSearchCondition.class));
        }

        @Test
        @DisplayName("[200] GET /api/books/popular - 쿼리 파라미터가 PopularBookSearchCondition으로 바인딩된다")
        void getPopularBooks_bindsQueryParamsToCondition() throws Exception {
            // given
            Instant after = Instant.parse("2026-01-21T00:00:00Z");
            CursorPageResponsePopularBookDto response = new CursorPageResponsePopularBookDto(
                    List.of(sampleDto(2L)),
                    "11",
                    after,
                    1,
                    2L,
                    true
            );
            given(popularBookService.searchPopularBooks(any(PopularBookSearchCondition.class)))
                    .willReturn(response);

            // when
            mockMvc.perform(get("/api/books/popular")
                            .queryParam("period", "DAILY")
                            .queryParam("direction", "ASC")
                            .queryParam("cursor", "10")
                            .queryParam("after", "2026-01-21T00:00:00Z")
                            .queryParam("limit", "50")
                    )
                    .andExpect(status().isOk());

            // then: service로 넘어간 condition을 캡쳐해서 바인딩 값 검증
            ArgumentCaptor<PopularBookSearchCondition> captor = ArgumentCaptor.forClass(PopularBookSearchCondition.class);
            then(popularBookService).should().searchPopularBooks(captor.capture());

            PopularBookSearchCondition condition = captor.getValue();
            assertThat(condition.period()).isEqualTo(PeriodType.DAILY);
            assertThat(condition.direction()).isEqualTo(SortDirection.ASC);
            assertThat(condition.cursor()).isEqualTo("10");
            assertThat(condition.after()).isEqualTo(after);
            assertThat(condition.limit()).isEqualTo(50);
        }

        @Test
        @DisplayName("[200] GET /api/books/popular - after가 date-time 형식이 아니면 400 Bad Request")
        void getPopularBooks_invalidAfter_returns400() throws Exception {
            // when & then
            mockMvc.perform(get("/api/books/popular")
                            .queryParam("after", "not-a-datetime"))
                    .andExpect(status().isBadRequest());
        }

        private PopularBookDto sampleDto(long rank) {
            // PopularBookDto 생성자/필드에 맞춰 조정해줘
            return new PopularBookDto(
                    UUID.randomUUID(),         // popularBookId
                    UUID.randomUUID(),         // bookId
                    "title",
                    "author",
                    "thumb",
                    PeriodType.DAILY,
                    rank,
                    99.0,
                    10L,
                    4.8,
                    Instant.parse("2026-01-21T00:00:00Z")
            );
        }
    }
}
