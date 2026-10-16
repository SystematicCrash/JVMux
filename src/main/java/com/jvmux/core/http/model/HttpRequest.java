package com.jvmux.core.http.model;

import lombok.Builder;

import java.util.Map;

@Builder
public record HttpRequest(
        RequestLine startLine,
        Map<String, String> headers
) {}
