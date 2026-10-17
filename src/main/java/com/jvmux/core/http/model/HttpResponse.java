package com.jvmux.core.http.model;

import com.jvmux.core.http.constant.HttpHeader;
import com.jvmux.core.http.constant.HttpVersion;
import lombok.Builder;

import java.util.Map;

@Builder
public record HttpResponse(
        HttpVersion version,
        int status,
        Map<HttpHeader, String> headers,
        String body
) {
    public static HttpResponse empty(int status) {
        return HttpResponse.builder()
                .version(HttpVersion.HTTP_1_1)
                .status(status)
                .headers(Map.of(
                        HttpHeader.HOST, "JVMux",
                        HttpHeader.CONTENT_LENGTH, "0"
                )).build();
    }

    public static HttpResponse text(int status, String text) {
        return HttpResponse.builder()
                .version(HttpVersion.HTTP_1_1)
                .status(status)
                .headers(Map.of(
                        HttpHeader.HOST, "JVMux",
                        HttpHeader.CONTENT_LENGTH, String.valueOf(text.length()),
                        HttpHeader.CONTENT_TYPE, "text/plain"
                ))
                .body(text)
                .build();
    }
}
