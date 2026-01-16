package com.deokhugam.deokhugam.infrastructure.unit.component;

import com.deokhugam.infrastructure.storage.S3FileStorage;
import com.deokhugam.infrastructure.storage.exception.S3.S3FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@ExtendWith(MockitoExtension.class)
class S3FileStorageTest {

    @InjectMocks
    private S3FileStorage s3FileStorage;

    @Mock
    private S3Client s3Client;

    private final String BUCKET_NAME = "test-bucket";
    private final String CDN_DOMAIN = "https://cdn.deokhugam.com";

    @BeforeEach
    void setUp() {
        // @Value 필드에 값 주입 (Unit Test이므로 리플렉션 사용)
        setField(s3FileStorage, "bucketName", BUCKET_NAME);
        setField(s3FileStorage, "cloudFrontDomain", CDN_DOMAIN);
    }

    @Nested
    @DisplayName("파일 업로드 (Upload)")
    class Upload {
        @Test
        @DisplayName("[Behavior][Positive] 알맞은 메타데이터(Cache-Control 등)와 함께 putObject가 호출되어야 한다")
        void upload_success() {
            // given
            String fileName = UUID.randomUUID().toString();
            String key = "books/" + fileName+".jpg";
            MockMultipartFile file = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());

            // when
            String resultKey = s3FileStorage.upload(file, key);

            // then
            assertThat(resultKey).isEqualTo(key);
            ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
            verify(s3Client).putObject(captor.capture(), any(RequestBody.class));

            PutObjectRequest request = captor.getValue();
            assertThat(request.bucket()).isEqualTo(BUCKET_NAME);
            assertThat(request.key()).isEqualTo(key);
            assertThat(request.contentType()).isEqualTo("image/jpeg");
            assertThat(request.cacheControl()).isEqualTo("max-age=31536000");
        }

        @Test
        @DisplayName("[Behavior][Negative] AWS S3 오류 발생 시 S3FileStorageException으로 변환하여 던져야 한다")
        void upload_fail_aws_error() {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

            given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .willThrow(S3Exception.builder().message("AWS Error").build());

            // when & then
            assertThatThrownBy(() -> s3FileStorage.upload(file, "key"))
                    .isInstanceOf(S3FileStorageException.class);
        }
    }
}