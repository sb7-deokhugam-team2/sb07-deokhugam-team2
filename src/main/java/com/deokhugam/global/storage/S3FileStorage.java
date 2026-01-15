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
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class S3FileStorage extends BaseFileStorage{

    private final S3Client s3Client;

    @Value("${storage.app.aws.cloud-front.domain}")
    private String cloudFrontDomain;

    @Value("${storage.app.aws.s3.bucket}")
    private String bucketName;



    @Override
    public String upload(MultipartFile file, String fileKey) {
        if (file.isEmpty()) {
            log.warn("업로드 실패: 빈 파일이 전달됨");
            throw new S3FileStorageException(ErrorCode.EMPTY_FILE_EXCEPTION);
        }

        String contentType = file.getContentType();
        validateContentType(contentType);

        String originalFilename = file.getOriginalFilename();

        log.info("S3 업로드 시작 - 파일명: {}, 크기: {} bytes, 타입: {}", originalFilename, file.getSize(), contentType);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .cacheControl("max-age=31536000")
                    .build();

            s3Client.putObject(request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("S3 업로드 성공 - Key: {}", fileKey);
            return fileKey;
        } catch (IOException e) {
            log.error("S3 업로드 중 IO 오류 발생 - 파일명: {}", originalFilename, e);
            throw new S3FileStorageException(ErrorCode.IO_EXCEPTION_ON_UPLOAD);
        } catch (RuntimeException e) {
            log.error("S3 업로드 중 AWS SDK 오류 발생 - Key: {}", fileKey, e);
            throw new S3FileStorageException(ErrorCode.PUT_OBJECT_EXCEPTION);
        }
    }

    @Override
    public String generateUrl(String key) {
        if (key == null || key.isBlank()) return null;

        return cloudFrontDomain + "/" + key;
    }

    @Override
    public void delete(String keyFromDb) {
        if (keyFromDb == null || keyFromDb.isBlank()) return;

        String realKey = keyFromDb.split("\\?")[0];

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(realKey)
                    .build());
            log.info("S3 파일 삭제 완료 - Key: {}", realKey);
        } catch (S3Exception e){
            log.error("S3 파일 삭제 실패 (AWS 에러) - Key: {}, Code: {}, Msg: {}",
                    realKey, e.statusCode(), e.awsErrorDetails().errorMessage(), e);
            throw new S3FileStorageException(ErrorCode.FAIL_TO_DELETE_FILE);
        }
        catch (Exception e) {
            log.error("S3 파일 삭제 중 알 수 없는 오류 발생 - Key: {}", realKey, e);
            throw new S3FileStorageException(ErrorCode.FAIL_TO_DELETE_FILE);
        }
    }




}
