package com.jvmux.core.http.model;

import com.jvmux.core.http.constant.HttpMethod;
import com.jvmux.core.http.constant.HttpVersion;
import lombok.Builder;

import java.util.Map;

@Builder
public record HttpRequest(
        String path,
        HttpMethod method,
        HttpVersion version,
        Map<String, String> headers
) {}
