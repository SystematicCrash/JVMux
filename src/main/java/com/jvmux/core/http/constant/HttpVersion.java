package com.jvmux.core.http.constant;

public enum HttpVersion {
    HTTP_1_1("HTTP/1.1"),
    HTTP_2("HTTP/2"),
    HTTP_3("HTTP/3");

    private final String value;

    HttpVersion(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static HttpVersion from(String value) {
        return HttpVersion.valueOf(value.trim().toUpperCase());
    }
}
