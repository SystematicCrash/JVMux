package com.jvmux.core.http.model;

import com.jvmux.core.http.constant.HttpHeader;
import com.jvmux.core.http.parser.RequestParser;
import lombok.Builder;

import java.io.InputStream;
import java.util.Map;

@Builder
public record HttpRequest(
        RequestLine requestLine,
        Map<String, String> headers,
        InputStream body
) {
    public String getHeader(HttpHeader name) {
        return this.headers.get(name.value());
    }

    public static HttpRequest from(String[] raw, InputStream body) {
        return RequestParser.parse(raw, body);
    }
}

