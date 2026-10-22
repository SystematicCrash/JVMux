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

    public static HttpException internalError(String message, Throwable cause) {
        return new HttpException(500, message, cause);
    }

    public static HttpException badRequest(String message) {
        return new HttpException(400, message);
    }

    public static HttpException badRequest(String message, Throwable cause) {
        return new HttpException(400, message, cause);
    }

    public static HttpException notFound() {
        return new HttpException(404, "Requested url not found");
    }

    public static HttpException notFound(Throwable cause) {
        return new HttpException(404, "Requested url not found", cause);
    }

    public static HttpException methodNotAllowed() {
        return new HttpException(405, "Method not allowed for this url");
    }

    public static HttpException methodNotAllowed(Throwable cause) {
        return new HttpException(405, "Method not allowed for this url", cause);
    }

    public static HttpException headersToLarge() {
        return new HttpException(431, "Request Header Fields Too Large");
    }

    public static HttpException headersToLarge(Throwable cause) {
        return new HttpException(431, "Request Header Fields Too Large", cause);
    }

    public static HttpException badGateway() {
        return new HttpException(502, "Bad Gateway Error");
    }

    public static HttpException badGateway(Throwable cause) {
        return new HttpException(502, "Bad Gateway Error", cause);
    }

    public static HttpException badGateway(String message, Throwable cause) {
        return new HttpException(502, message, cause);
    }

    public static HttpException gatewayTimeout() {
        return new HttpException(504, "Gateway Timeout Error");
    }

    public static HttpException gatewayTimeout(Throwable cause) {
        return new HttpException(504, "Gateway Timeout Error", cause);
    }

    public static HttpException gatewayTimeout(String message, Throwable cause) {
        return new HttpException(504, message, cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}