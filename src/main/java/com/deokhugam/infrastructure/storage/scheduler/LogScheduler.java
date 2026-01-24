package com.deokhugam.infrastructure.storage.scheduler;

import com.deokhugam.infrastructure.storage.LogS3Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogScheduler {

    private final LogS3Storage logS3Storage;
    @Value("${log.path:./logs}")
    private String logDir;


    @Scheduled(cron = "0 0 * * * *")
    public void uploadPastHourLog() {
        File dir = new File(logDir);

        log.info("로그 업로드 시도: -> s3://{}/{}", dir.getName(), logDir);

        File[] logFiles = dir.listFiles((d, name) ->
                name.startsWith("app-") && name.endsWith(".log") && !name.equals("app.log")
        );

        if (logFiles != null) {
            Arrays.stream(logFiles).forEach(logS3Storage::uploadLog);
        }
    }
}
