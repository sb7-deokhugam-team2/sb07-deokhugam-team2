package com.deokhugam.deokhugam.infrastructure.integration.component.search;

import com.deokhugam.domain.book.exception.BookISBNNotFoundException;
import com.deokhugam.infrastructure.search.book.NaverBookApiClient;
import com.deokhugam.infrastructure.search.book.dto.BookGlobalApiDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Tag("integration")
class NaverBookApiClientIntegrationTest {

    @Autowired
    private NaverBookApiClient naverBookApiClient;

    @Test
    @DisplayName("실제 네이버 API를 호출하여 도서 정보와 이미지를 정상적으로 가져와야 한다")
    void get_data_success_integration_test() {
        // given
        String realIsbn = "9788966260959";

        // when
        BookGlobalApiDto result = naverBookApiClient.getData(realIsbn);

        // then
        assertThat(result).isNotNull();

        // 1. 도서 정보 검증
        assertThat(result.isbn()).isEqualTo(realIsbn);
        assertThat(result.title()).contains("Clean Code");
        assertThat(result.publishedDate()).isNotNull();

        // 2. 이미지 다운로드 검증 (실제 바이트 배열이 들어왔는지)
        assertThat(result.thumbnailImage()).isNotNull();
        assertThat(result.thumbnailImage().length).isGreaterThan(0);

        System.out.println("Integration Test Result: " + result.title() + " / Image Size: " + result.thumbnailImage().length + " bytes");
    }

    @Test
    @DisplayName("존재하지 않는 ISBN으로 요청 시 네이버 API는 빈 결과를 반환하고 예외가 발생해야 한다")
    void get_data_not_found_integration_test() {
        // given
        // 네이버에 없을 법한 임의의 ISBN
        String invalidIsbn = "9780000000000";

        // when & then
        assertThatThrownBy(() -> naverBookApiClient.getData(invalidIsbn))
                .isInstanceOf(BookISBNNotFoundException.class);
    }

    @Test
    @DisplayName("검색된 도서 데이터의 형식이(날짜, URL 등) 유효한지 검증한다")
    void data_format_validation_test() {
        // given
        String isbn = "9788966260959"; // Clean Code

        // when
        BookGlobalApiDto result = naverBookApiClient.getData(isbn);

        // then
        assertThat(result.publishedDate()).isBeforeOrEqualTo(LocalDate.now());

        assertThat(result.title()).isNotEmpty();

        assertThat(result.thumbnailImage()).isNotEmpty();
    }
}
