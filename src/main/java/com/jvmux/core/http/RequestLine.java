package com.jvmux.core.http;

import com.jvmux.core.http.constant.HttpMethod;
import com.jvmux.core.http.constant.HttpVersion;
import lombok.Builder;

@Builder
public record RequestLine(
        String path,
        HttpMethod method,
        HttpVersion version
) {}
