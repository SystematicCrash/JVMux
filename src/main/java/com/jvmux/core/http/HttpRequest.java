package com.jvmux.core.http;

import lombok.Builder;

import java.util.Map;

@Builder
public record HttpRequest(
        String path,
        String method,
        String version,
        Map<String, String> headers
) {}
