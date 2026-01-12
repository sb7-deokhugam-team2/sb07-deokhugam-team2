package com.deokhugam.global.storage;

import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.global.storage.exception.S3.S3FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3FileStorage extends BaseFileStorage{
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.aws.s3.bucket}")
    private String bucketName;



    @Override
    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            log.warn("업로드 실패: 빈 파일이 전달됨");
            throw new S3FileStorageException(ErrorCode.EMPTY_FILE_EXCEPTION);
        }

        String contentType = file.getContentType();
        validateContentType(contentType);

        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        log.info("S3 업로드 시작 - 파일명: {}, 크기: {} bytes, 타입: {}", originalFilename, file.getSize(), contentType);

        String key = "books/" + createStoreFileName(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("S3 업로드 성공 - Key: {}", key);
            return key;
        } catch (IOException e) {
            log.error("S3 업로드 중 IO 오류 발생 - 파일명: {}", originalFilename, e);
            throw new S3FileStorageException(ErrorCode.IO_EXCEPTION_ON_UPLOAD);
        } catch (RuntimeException e) {
            log.error("S3 업로드 중 AWS SDK 오류 발생 - Key: {}", key, e);
            throw new S3FileStorageException(ErrorCode.PUT_OBJECT_EXCEPTION);
        }
    }

    @Override
    public String generateUrl(String key) {
        if (key == null || key.isBlank()) return null;

        try {
            GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(b -> b.bucket(bucketName).key(key))
                    .build();

            return s3Presigner.presignGetObject(request).url().toString();
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패 - Key: {}", key, e);
            throw new S3FileStorageException(ErrorCode.FAIL_TO_GENERATE_URL);
        }
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) return;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            log.info("S3 파일 삭제 완료 - Key: {}", key);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패 (고아 객체 수동 확인 필요) - Key: {}", key, e);
        }
    }




}
