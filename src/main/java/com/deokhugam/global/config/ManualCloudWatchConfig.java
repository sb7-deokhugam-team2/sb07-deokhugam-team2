package com.deokhugam.global.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

import java.util.Map;

@Configuration
public class ManualCloudWatchConfig {

    private final Logger log = LoggerFactory.getLogger(ManualCloudWatchConfig.class);

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        // AWS 클라이언트를 강제로 생성합니다. (자격증명, 리전 명시)
        return CloudWatchAsyncClient.builder()
                .region(Region.AP_NORTHEAST_2) // 서울 리전 강제 고정
                .credentialsProvider(DefaultCredentialsProvider.create()) // ECS 역할(Role) 가져오기
                .build();
    }

    @Bean
    public MeterRegistry cloudWatchMeterRegistry(CloudWatchAsyncClient cloudWatchAsyncClient) {
        log.info("=================================================");
        log.info("🚀 [강제 실행] CloudWatch MeterRegistry 생성 시작");
        log.info("=================================================");

        CloudWatchConfig cloudWatchConfig = new CloudWatchConfig() {
            private final Map<String, String> configuration = Map.of(
                    "cloudwatch.namespace", "deokhugam-monitoring", // 네임스페이스 강제 설정
                    "cloudwatch.step", "1m",                        // 1분 단위
                    "cloudwatch.batchSize", "20"
            );

            @Override
            public String get(String key) {
                return configuration.get(key);
            }
        };

        CloudWatchMeterRegistry registry = new CloudWatchMeterRegistry(
                cloudWatchConfig,
                Clock.SYSTEM,
                cloudWatchAsyncClient
        );

        // 생성 직후 테스트 로그 찍기
        registry.config().commonTags("env", "prod");
        log.info("✅ CloudWatch 레지스트리 생성 완료! (이제 데이터가 안 가면 권한 문제입니다)");

        return registry;
    }
}