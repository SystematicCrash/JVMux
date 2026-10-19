package com.jvmux.network.http.model;

import com.jvmux.network.http.constant.HttpHeader;
import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.io.BodyWriter;
import com.jvmux.network.http.io.ChunkedBodyWriter;
import com.jvmux.network.http.io.FixedBodyWriter;
import lombok.Builder;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

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
        if (isChunked()) {
            return new ChunkedBodyWriter(this.body);
        }
        if (isFixedSize()) {
            return new FixedBodyWriter(this.body, getContentLength());
        }
        return null;
    }

    private boolean isChunked() {
        return Objects.equals(this.getHeader(HttpHeader.TRANSFER_ENCODING), "chunked");
    }

    private boolean isFixedSize() {
        return this.getHeader(HttpHeader.CONTENT_LENGTH) != null;
    }

    private long getContentLength() {
        var contentLen = getHeader(HttpHeader.CONTENT_LENGTH);
        return Long.parseLong(contentLen);
    }
}

