package com.deokhugam.deokhugam.global.integration.component;

import com.deokhugam.global.storage.FileStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// 실제 스프링 컨텍스트를 다 띄워서 yml 설정(실제 AWS Key)을 읽어옵니다.
@SpringBootTest
@Tag("integration") // 전체 테스트 돌릴 때 제외하고 싶다면 태그 사용
class S3FileStorageIntegrationTest {

    @Autowired
    private FileStorage fileStorage;

    @Autowired
    private S3Client s3Client;

    @Value("${storage.app.aws.cloud-front.domain}")
    private String cloudFrontDomain;

    @Value("${storage.app.aws.s3.bucket}")
    private String bucketName;

    @Test
    @DisplayName("실제 AWS S3에 파일 업로드 및 삭제가 정상적으로 수행되어야 한다")
    void upload_and_delete_integration_test() throws IOException {
        //given
        String uniqueFileName = "test-integration-" + UUID.randomUUID() + ".txt";
        String key = "test-folder/" + uniqueFileName;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                uniqueFileName,
                "text/plain",
                "Integration Test".getBytes()
        );

        System.out.println("Testing with Key: " + key);

        //when & then
        String uploadedKey = fileStorage.upload(file, key);


        assertThat(uploadedKey).isEqualTo(key);

        boolean exists = doesObjectExist(key, bucketName);
        assertThat(exists).isTrue().as("파일이 실제 S3 버킷에 존재해야 합니다.");

        //when & then
        fileStorage.delete(key);

        boolean existsAfterDelete = doesObjectExist(key, bucketName);
        assertThat(existsAfterDelete).isFalse().as("삭제 후에는 S3 버킷에서 파일이 없어야 합니다.");
    }

    @Test
    @DisplayName("S3에 업로드된 파일의 Content-Type과 Cache-Control이 정상적으로 설정되어야 한다")
    void upload_file_metadata_check_test() {
        // given
        String contentType = "image/jpeg";
        String uniqueFileName = "metadata-test-" + UUID.randomUUID() + ".jpg";
        String key = "test-folder/" + uniqueFileName;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                uniqueFileName,
                contentType,
                "dummy image content".getBytes()
        );

        // when
        fileStorage.upload(file, key);

        HeadObjectResponse metadata = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());

        assertThat(metadata.contentType()).isEqualTo(contentType);
        assertThat(metadata.cacheControl()).isEqualTo("max-age=31536000");

        fileStorage.delete(key);
    }

    @Test
    @DisplayName("존재하지 않는 파일을 삭제해도 예외가 발생하지 않아야 한다 (멱등성 보장)")
    void delete_non_existent_file_test() {
        // given
        String nonExistentKey = "test-folder/ghost-file-" + UUID.randomUUID() + ".txt";

        // when & then
        try {
            fileStorage.delete(nonExistentKey);
        } catch (Exception e) {
            org.assertj.core.api.Assertions.fail("없는 파일을 삭제할 때 예외가 발생하면 안됩니다: " + e.getMessage());
        }
    }

    private boolean doesObjectExist(String key, String bucketName) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }


}
