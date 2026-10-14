package com.jvmux.core.http;

public record RequestLine(
        String path,
        String version,
        String method
) {}
