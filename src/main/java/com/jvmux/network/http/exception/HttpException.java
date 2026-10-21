package com.jvmux.network.http.exception;

import lombok.Getter;

@Getter
public class HttpException extends RuntimeException {
    private final int status;

    public HttpException(int status, String message) {
        super(message);
        this.status = status;
    }

    public HttpException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public static HttpException internalError() {
        return new HttpException(500, "Internal Server Error");
    }

    public static HttpException internalError(Throwable cause) {
        return new HttpException(500, "Internal Server Error", cause);
    }

    public static HttpException badRequest(String message) {
        return new HttpException(400, message);
    }

    public static HttpException headersToLarge() {
        return new HttpException(431, "Request Header Fields Too Large");
    }

    public static HttpException badGateway() {
        return new HttpException(502, "Bad Gateway Error");
    }

    public static HttpException notFound() {
        return new HttpException(404, "Requested url not found");
    }

    public static HttpException methodNotAllowed() {
        return new HttpException(405, "Method now allowed for this url");
    }

    public static HttpException gatewayTimeout() {
        return new HttpException(504, "Gateway Timeout Error");
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
