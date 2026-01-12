package com.deokhugam.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    String upload(MultipartFile file);

    String generateUrl(String key);

    void delete(String key);

}
