package com.deokhugam.infrastructure.storage.scheduler;

import com.deokhugam.infrastructure.storage.LogS3Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class LogScheduler {

    private final LogS3Storage logS3Storage; // 👈 새로 만든 클래스 주입
    private static final String LOG_DIR = "./.logs";

    @Scheduled(cron = "0 1 * * * *")
    public void uploadPastHourLog() {
        File dir = new File(LOG_DIR);

        File[] logFiles = dir.listFiles((d, name) ->
                name.startsWith("app-") && name.endsWith(".log") && !name.equals("app.log")
        );

        if (logFiles != null) {
            Arrays.stream(logFiles).forEach(logS3Storage::uploadLog); // 👈 호출
        }
    }
}
