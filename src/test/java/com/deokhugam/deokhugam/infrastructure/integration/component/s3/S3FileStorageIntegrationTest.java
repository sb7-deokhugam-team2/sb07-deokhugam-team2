package com.deokhugam.deokhugam.infrastructure.integration.component.s3;

import com.deokhugam.infrastructure.storage.FileStorage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Disabled
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

    @Test
    @DisplayName("S3에 파일을 업로드하고 다운로드했을 때 데이터가 손상 없이 일치해야 한다")
    void upload_download_consistency_test() throws IOException {
        // given
        String uniqueFileName = "integrity-test-" + UUID.randomUUID() + ".txt";
        String key = "test-folder/" + uniqueFileName;
        byte[] originalContent = "Hello World Integration Test".getBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file", uniqueFileName, "text/plain", originalContent
        );

        try {
            // when
            fileStorage.upload(file, key);

            // then
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucketName).key(key).build()
            );
            byte[] downloadedContent = objectBytes.asByteArray();

            assertThat(downloadedContent).isEqualTo(originalContent);

        } finally {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
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
