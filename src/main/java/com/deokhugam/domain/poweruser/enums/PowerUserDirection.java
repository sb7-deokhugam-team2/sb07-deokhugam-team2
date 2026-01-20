package com.deokhugam.domain.poweruser.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PowerUserDirection {
    ASC("asc"),
    DESC("desc");

    private final String value;

    public static PowerUserDirection from(String source) {
        return Arrays.stream(values())
                .filter(direction -> direction.value.equalsIgnoreCase(source))
                .findFirst()
                .orElse(DESC);
    }
}
