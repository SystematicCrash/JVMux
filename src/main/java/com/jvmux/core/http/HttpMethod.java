package com.jvmux.core.http;

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

    @Override
    public String toString() {
        return this.value;
    }
}
