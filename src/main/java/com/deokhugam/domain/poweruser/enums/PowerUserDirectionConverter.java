package com.deokhugam.domain.poweruser.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PowerUserDirectionConverter implements Converter<String, PowerUserDirection> {
    @Override
    public PowerUserDirection convert(String source) {
        return source.isBlank() ? PowerUserDirection.DESC : PowerUserDirection.from(source);
    }
}