package com.deokhugam.global.storage;

import org.springframework.web.multipart.MultipartFile;


public interface FileStorage {

    String upload(MultipartFile file, String fileKey);

    String generateUrl(String key);

    void delete(String key);

}
