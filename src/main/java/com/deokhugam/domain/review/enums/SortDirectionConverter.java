package com.deokhugam.domain.review.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SortDirectionConverter implements Converter<String, SortDirection> {
    @Override
    public SortDirection convert(String source) {
        if (source == null) {
            return SortDirection.DESC;
        }
        return SortDirection.valueOf(source.toUpperCase());
    }
}
