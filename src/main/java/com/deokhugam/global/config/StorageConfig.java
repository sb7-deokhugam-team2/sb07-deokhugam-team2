package com.deokhugam.global.config;

import com.deokhugam.global.storage.FileStorage;
import com.deokhugam.global.storage.S3FileStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Slf4j
public class StorageConfig {

    @Configuration
    @ConditionalOnProperty(name = "storage.type", havingValue = "s3")
    public static class S3ConnectionConfig {
        @Value("${storage.app.aws.credentials.AWS_ACCESS_KEY}")
        private String accessKey;

        @Value("${storage.app.aws.credentials.AWS_SECRET_KEY}")
        private String secretKey;

        @Value("${storage.app.aws.region.static}")
        private String region;
        @Bean
        public S3Client s3Client() {
            log.info("Storage Type is S3. Initializing S3Client... Region: {}", region);
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    ))
                    .build();
        }

        @Bean
        public FileStorage fileStorage(S3Client s3Client) {
            log.info("Storage Type is S3. S3FileStorage가 등록됩니다.");
            return new S3FileStorage(s3Client);
        }
    }

}
