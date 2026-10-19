package com.jvmux.network.http.constant;

public final class HttpStatus {

    private HttpStatus() {}

    public static boolean isValid(int status) {
        return status <= 599 && status >= 100;
    }

    public static String reasonPhrase(int statusCode) {
        return switch (statusCode) {
            // 1xx Informational
            case 100 -> "Continue";
            case 101 -> "Switching Protocols";

            // 2xx Success
            case 200 -> "OK";
            case 201 -> "Created";
            case 202 -> "Accepted";
            case 204 -> "No Content";

            // 3xx Redirection
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 304 -> "Not Modified";
            case 307 -> "Temporary Redirect";
            case 308 -> "Permanent Redirect";

            // 4xx Client Errors
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 408 -> "Request Timeout";
            case 409 -> "Conflict";
            case 413 -> "Content Too Large";
            case 414 -> "URI Too Long";
            case 429 -> "Too Many Requests";
            case 431 -> "Request Header Fields Too Large";

            // 5xx Server Errors
            case 500 -> "Internal Server Error";
            case 501 -> "Not Implemented";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            case 505 -> "HTTP Version Not Supported";

            // fallback
            default -> "Unknown Status";
        };
    }
}