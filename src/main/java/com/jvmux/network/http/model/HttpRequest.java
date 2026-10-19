package com.jvmux.network.http.model;

import com.jvmux.network.http.constant.HttpHeader;
import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.io.BodyWriter;
import com.jvmux.network.http.io.BodyWriterFactory;
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

    public BodyWriter getWriter() {
        return BodyWriterFactory.create(this.body, this.headers);
    }
}

