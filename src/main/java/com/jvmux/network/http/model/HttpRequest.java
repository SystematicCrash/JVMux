package com.jvmux.network.http.model;

import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.io.RequestParser;
import lombok.Builder;

import java.io.InputStream;
import java.util.Map;

@Builder
public record HttpRequest(
        String path,
        HttpMethod method,
        HttpVersion version,
        Map<String, String> headers,
        InputStream body
) {
    public String getHeader(String name) {
        return this.headers.get(name);
    }

    public static HttpRequest from(String[] raw, InputStream body) {
        return RequestParser.parse(raw, body);
    }
}

