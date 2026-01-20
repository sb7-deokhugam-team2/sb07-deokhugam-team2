package com.deokhugam.domain.book.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BookThumbnailUrlResolver {
    private final String cloudFrontDomain;

    public BookThumbnailUrlResolver(@Value("${storage.app.aws.cloud-front.domain}") String cloudFrontDomain) {
        this.cloudFrontDomain = trimTrailingSlash(cloudFrontDomain);
    }

    public String toFullUrl(String key) {
        if (key == null || key.isBlank()) return null;
        if (key.startsWith("http://") || key.startsWith("https://")) return key;
        return cloudFrontDomain + "/" + key;
    }

    private static String trimTrailingSlash(String url) {
        return (url != null && url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
    }
}
