package com.deokhugam.global.storage;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

public interface FileStorage {

    String upload(MultipartFile file, String fileKey);

    String generateUrl(String key);

    void delete(String key);

}
