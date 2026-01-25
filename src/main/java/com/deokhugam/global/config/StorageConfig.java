package com.deokhugam.global.config;

import com.deokhugam.infrastructure.storage.FileStorage;
import com.deokhugam.infrastructure.storage.S3FileStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Slf4j
public class StorageConfig {

    @Configuration
    @ConditionalOnProperty(name = "storage.type", havingValue = "s3")
    public static class S3ConnectionConfig {

        @Value("${storage.app.aws.region.static}")
        private String region;

        @Bean
        @Primary
        public S3Client s3Client(
                @Value("${storage.app.aws.credentials.AWS_ACCESS_KEY}") String accessKey,
                @Value("${storage.app.aws.credentials.AWS_SECRET_KEY}") String secretKey) {

            log.info("Initializing Primary S3Client for Images... Region: {}", region);
            return createS3Client(accessKey, secretKey, region);
        }

        @Bean(name = "logS3Client")
        public S3Client logS3Client(
                @Value("${storage.app.aws.log-credentials.AWS_ACCESS_KEY}") String accessKey,
                @Value("${storage.app.aws.log-credentials.AWS_SECRET_KEY}") String secretKey) {

            log.info("Initializing Secondary S3Client for Logs... Region: {}", region);
            return createS3Client(accessKey, secretKey, region);
        }

        @Bean
        public FileStorage fileStorage(S3Client s3Client) {
            log.info("S3FileStorage가 등록됩니다. (Primary S3Client 사용)");
            return new S3FileStorage(s3Client);
        }

        private S3Client createS3Client(String accessKey, String secretKey, String region) {
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    ))
                    .build();
        }
    }
}
