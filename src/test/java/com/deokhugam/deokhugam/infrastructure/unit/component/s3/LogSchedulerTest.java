package com.deokhugam.deokhugam.infrastructure.unit.component.s3;

import com.deokhugam.infrastructure.storage.LogS3Storage;
import com.deokhugam.infrastructure.storage.scheduler.LogScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogSchedulerTest {

    @Mock
    private LogS3Storage logS3Storage;

    @InjectMocks
    private LogScheduler logScheduler;

    @TempDir
    Path tempDir; // 임시 폴더

    @Test
    @DisplayName("스케줄러는 app.log는 건너뛰고 롤링된 파일만 업로드해야 한다")
    void uploadPastHourLog_FilterCheck() throws IOException {
        // given
        ReflectionTestUtils.setField(logScheduler, "logDir", tempDir.toString());

        // 파일 생성
        File targetFile = Files.createFile(tempDir.resolve("app-2024-01-01-15.log")).toFile();
        File currentFile = Files.createFile(tempDir.resolve("app.log")).toFile();
        File otherFile = Files.createFile(tempDir.resolve("error.txt")).toFile();

        // when
        logScheduler.uploadPastHourLog();

        // then
        verify(logS3Storage, times(1)).uploadLog(argThat(file ->
                file.getName().equals("app-2024-01-01-15.log")
        ));

        verify(logS3Storage, times(0)).uploadLog(argThat(file ->
                file.getName().equals("app.log")
        ));

        verify(logS3Storage, times(1)).uploadLog(any(File.class));
    }
}
