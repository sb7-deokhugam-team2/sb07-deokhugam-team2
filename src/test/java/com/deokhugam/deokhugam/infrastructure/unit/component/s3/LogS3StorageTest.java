package com.deokhugam.deokhugam.infrastructure.unit.component.s3;

import com.deokhugam.infrastructure.storage.LogS3Storage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class LogS3StorageTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private LogS3Storage logS3Storage;

    @TempDir
    Path tempDir; // 테스트가 끝나면 자동으로 사라지는 임시 폴더

    @Test
    @DisplayName("로그 파일 업로드 성공 시 S3 호출 후 로컬 파일이 삭제되어야 한다")
    void uploadLog_Success() throws IOException {
        // given
        File tempFile = Files.createFile(tempDir.resolve("app-2024-01-01-15.log")).toFile();

        ReflectionTestUtils.setField(logS3Storage, "logBucket", "test-bucket");
        ReflectionTestUtils.setField(logS3Storage, "profile", "test");

        // when
        logS3Storage.uploadLog(tempFile);

        // then
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String expectedKey = String.format("logs/test/%s/%s", today, tempFile.getName());

        verify(s3Client, times(1)).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );

        assertThat(tempFile.exists()).isFalse();
    }

    @Test
    @DisplayName("파일이 존재하지 않으면 아무 일도 일어나지 않아야 한다")
    void uploadLog_FileNotFound() {
        // given
        File nonExistentFile = new File("ghost.log");

        // when
        logS3Storage.uploadLog(nonExistentFile);

        // then
        verify(s3Client, times(0)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}
