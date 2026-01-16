package com.deokhugam.infrastructure.ocr;

import org.springframework.web.multipart.MultipartFile;

public interface IsbnExtractor {
    String extractIsbn(MultipartFile image);
}
