package com.deokhugam.deokhugam.infrastructure.integration.component.ocr;

import com.deokhugam.infrastructure.ocr.OCRApiClient;
import com.deokhugam.infrastructure.ocr.exception.OCRFileEmptyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Tag("external-api")
class OCRApiClientIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(OCRApiClientIntegrationTest.class);
    @Autowired
    private OCRApiClient ocrApiClient;

    @Test
    @DisplayName("실제 OCR API를 호출하여 이미지에서 ISBN을 정상적으로 추출해야 한다")
    void extract_isbn_success_integration_test() throws IOException {
        // given
        String expectedIsbn = "9788966260959"; // Clean Code ISBN
        String fileName = "Test_ISBN.png";

        ClassPathResource resource = new ClassPathResource("images/" + fileName);

        if (!resource.exists()) {
            System.out.println("⚠️ 테스트용 이미지가 없어 실제 API 호출 테스트를 건너뜁니다: " + fileName);
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    fileName,
                    "image/jpeg",
                    inputStream
            );

            // when
            String resultIsbn = ocrApiClient.extractIsbn(file);

            // then
            assertThat(resultIsbn).isNotNull();
            assertThat(resultIsbn).contains(expectedIsbn);

            System.out.println("Integration Test Result: Extracted ISBN = " + resultIsbn);
        }
    }

    @Test
    @DisplayName("ISBN이 없는 이미지를 요청하면 null을 반환해야 한다 (API 통신은 성공)")
    void extract_isbn_not_found_integration_test() throws IOException {
        // given
        String fileName = "no_isbn_image.jpg";
        ClassPathResource resource = new ClassPathResource("images/" + fileName);

        if (!resource.exists()) {
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    fileName,
                    "image/jpeg",
                    inputStream
            );

            // when
            String resultIsbn = ocrApiClient.extractIsbn(file);

            // then
            assertThat(resultIsbn).isNull();
        }
    }

    @Test
    @DisplayName("빈 파일을 요청 시 API 호출 전에 예외가 발생해야 한다")
    void extract_isbn_empty_file_test() {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // when & then
        assertThatThrownBy(() -> ocrApiClient.extractIsbn(emptyFile))
                .isInstanceOf(OCRFileEmptyException.class);
    }
}
