package com.petproject.lalrparser.shared;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor
public final class Mapper {
    private final static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setDefaultPrettyPrinter(new DefaultPrettyPrinter());
    }

    @SneakyThrows
    public static String writeValueAsString(Object o) {
        return mapper.writeValueAsString(o);
    }

    public static ObjectMapper get() {
        return mapper;
    }
}
