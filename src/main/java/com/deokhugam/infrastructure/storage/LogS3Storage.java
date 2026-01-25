package com.deokhugam.infrastructure.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class LogS3Storage {
    private final S3Client s3Client;

    @Value("${storage.app.aws.s3.log-bucket}")
    private String logBucket;

    @Value("${spring.profiles.active:unknown}")
    private String profile;

    public LogS3Storage(@Qualifier("logS3Client") S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void uploadLog(File file) {
        if (!file.exists()) return;

        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String key = String.format("logs/%s/%s/%s", profile, datePath, file.getName());

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(logBucket)
                    .key(key)
                    .build();

            s3Client.putObject(request, RequestBody.fromFile(file));

            log.info("로그 업로드에 성공했습니다.: {} -> s3://{}/{}", file.getName(), logBucket, key);

            if (file.delete()) {
                log.debug("로컬 로그가 삭제 되었습니다.: {}", file.getName());
            }

        } catch (Exception ignore) {
            log.error("로그 적재 실패, 로컬 기록 확인 필요합니다.: {}", file.getName(), ignore);
        }
    }
}
