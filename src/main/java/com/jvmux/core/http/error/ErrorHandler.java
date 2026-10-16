package com.jvmux.core.http.error;

import com.jvmux.core.http.constant.HttpHeader;
import com.jvmux.core.http.constant.HttpVersion;
import com.jvmux.core.http.model.HttpResponse;

import java.util.Map;

public final class ErrorHandler {

    private static final int DEFAULT_STATUS = 500;

    private ErrorHandler() {}

    public static HttpResponse toResponse(Throwable error) {
        return HttpResponse.builder()
                .version(HttpVersion.HTTP_1_1)
                .status(statusOf(error))
                .headers(errorHeaders())
                .build();
    }

    private static int statusOf(Throwable error) {
        if (error instanceof HttpException http) {
            return http.getStatus();
        }
        return DEFAULT_STATUS;
    }

    private static Map<String, String> errorHeaders() {
        return Map.of(
                HttpHeader.CONTENT_LENGTH.value(), "0",
                HttpHeader.CONNECTION.value(), "close"
        );
    }
}
