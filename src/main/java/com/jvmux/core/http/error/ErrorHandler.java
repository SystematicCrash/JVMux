package com.jvmux.core.http.error;

import com.jvmux.core.http.constant.HttpHeader;
import com.jvmux.core.http.constant.HttpVersion;
import com.jvmux.core.http.model.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class ErrorHandler {

    private static final int DEFAULT_STATUS = 500;
    private static final String DEFAULT_MESSAGE = "Internal Server Error";
    private static final String CONTENT_TYPE = "text/plain";

    private ErrorHandler() {}

    public static HttpResponse toResponse(Throwable error) {
        return HttpResponse.text(statusOf(error), bodyOf(error));
    }

    private static int statusOf(Throwable error) {
        if (error instanceof HttpException http) {
            return http.getStatus();
        }
        return DEFAULT_STATUS;
    }

    private static String bodyOf(Throwable error) {
        if (error instanceof HttpException exception) {
            var message = exception.getMessage();
            if (message != null && !message.isEmpty()) {
                return message;
            }
        }
        return DEFAULT_MESSAGE;
    }
}
