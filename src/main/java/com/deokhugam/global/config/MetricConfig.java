package com.deokhugam.global.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.util.Map;

@Configuration
public class MetricConfig {

    private static final Logger log = LoggerFactory.getLogger(MetricConfig.class);

    public MetricConfig() {
        // 이 로그가 안 뜨거나, 앱이 안 죽으면 코드가 반영 안 된 것임!
        System.out.println("🔥🔥🔥 [확인사살] MetricConfig 클래스가 로딩되었습니다!!! 🔥🔥🔥");
    }

    @Bean
    public CloudWatchConfig cloudWatchConfig() {
        return new CloudWatchConfig() {
            private final Map<String, String> configuration = Map.of(
                    "cloudwatch.namespace", "deokhugam/monitoring/test",
                    "cloudwatch.step", "1m",
                    "cloudwatch.batchSize", "20"
            );

            @Override
            public String get(String key) {
                return configuration.get(key);
            }
        };
    }

    @Bean
    public MeterRegistry cloudWatchMeterRegistry(CloudWatchConfig config) {
        log.info("🔥 [디버깅] CloudWatchMeterRegistry 생성을 시도합니다...");
        try {
            CloudWatchAsyncClient client = CloudWatchAsyncClient.create();
            CloudWatchMeterRegistry registry = new CloudWatchMeterRegistry(config, Clock.SYSTEM, client);
            log.info("✅ [디버깅] CloudWatchMeterRegistry 생성 성공! (이제 1분 뒤 전송 로그를 기다리세요)");
            return registry;
        } catch (Throwable e) {
            log.error("❌ [디버깅] 생성 실패! 원인을 확인하세요:", e);
            throw e; // 앱을 일부러 죽여서 에러 로그를 확인
        }
    }
}