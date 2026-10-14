package com.jvmux.core.http.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum HttpMethod {
    ANY("ANY"),
    GET("GET"),
    PUT("PUT"),
    POST("POST"),
    HEAD("HEAD"),
    PATCH("PATCH"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS");

    private final String value;

    HttpMethod(String value) {
        this.value = value;
    }

    public static HttpMethod from(String value) {
        return HttpMethod.valueOf(value.trim().toUpperCase());
    }

    @Override
    public String toString() {
        return this.value;
    }
}
