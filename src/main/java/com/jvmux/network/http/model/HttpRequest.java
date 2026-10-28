package com.jvmux.network.http.model;

import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.body.BodyWriter;
import com.jvmux.network.http.body.BodyWriterFactory;
import lombok.Builder;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

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

    public boolean hasHeader(String name) {
        return this.headers.containsKey(name);
    }

    public Optional<BodyWriter> getWriter() {
        return BodyWriterFactory.create(this.body, this.headers);
    }
}

