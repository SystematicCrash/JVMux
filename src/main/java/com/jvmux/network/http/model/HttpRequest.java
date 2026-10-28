package com.jvmux.network.http.model;

import com.jvmux.network.http.body.BodyWriter;
import com.jvmux.network.http.body.BodyWriterFactory;
import com.jvmux.network.http.constant.HttpHeader;
import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Getter
public class HttpRequest {

    private final String path;
    private final HttpMethod method;
    private final HttpVersion version;
    private final Map<String, String> headers;
    private final InputStream body;

    @Builder
    public HttpRequest(
            String path,
            HttpMethod method,
            HttpVersion version,
            Map<String, String> headers,
            InputStream body
    ) {
        this.path = path;
        this.method = method;
        this.version = version != null ? version : HttpVersion.HTTP_1_1;
        this.headers = new HashMap<>(headers);
        this.body = body != null ? body : InputStream.nullInputStream();
    }

    public String getHeader(String name) {
        return this.headers.get(name);
    }

    public boolean hasHeader(String name) {
        return this.headers.containsKey(name);
    }

    public void setHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public boolean isCloseConnection() {
        String connection = getHeader(HttpHeader.CONNECTION);
        return "close".equalsIgnoreCase(connection);
    }

    public Optional<BodyWriter> getWriter() {
        return BodyWriterFactory.create(this.body, this.headers);
    }
}