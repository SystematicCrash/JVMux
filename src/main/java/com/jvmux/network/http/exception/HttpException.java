package com.jvmux.network.http.exception;

import lombok.Getter;

@Getter
public class HttpException extends RuntimeException {
    private final int status;

    public HttpException(int status, String message) {
        super(message);
        this.status = status;
    }

    public static HttpException internalError() {
        return new HttpException(500, "Internal Server Error");
    }

    public static HttpException badRequest(String message) {
        return new HttpException(400, message);
    }

    public static HttpException headersToLarge() {
        return new HttpException(431, "Request Header Fields Too Large");
    }

    public static HttpException badGateway(String details) {
        return new HttpException(502, "Bad Gateway: " + details);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
