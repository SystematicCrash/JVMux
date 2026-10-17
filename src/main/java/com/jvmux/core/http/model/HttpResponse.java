package com.jvmux.core.http.model;

import com.jvmux.core.http.constant.HttpVersion;
import lombok.Builder;

import java.util.Map;

@Builder
public record HttpResponse(
        HttpVersion version,
        int status,
        Map<String, String> headers
) {}
