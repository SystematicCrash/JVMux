package com.jvmux.core.http.model;

import lombok.Builder;

import java.io.InputStream;
import java.util.Map;

@Builder
public record HttpRequest(
        RequestLine requestLine,
        Map<String, String> headers,
        InputStream body
) {}
