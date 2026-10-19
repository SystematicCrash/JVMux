package com.jvmux.network.http.model;

import com.jvmux.network.http.body.BodyEncoder;
import com.jvmux.network.http.body.HtmlEncoder;
import com.jvmux.network.http.body.JsonEncoder;
import com.jvmux.network.http.body.PlainTextEncoder;
import com.jvmux.network.http.io.BodyWriter;
import com.jvmux.network.http.io.ChunkedBodyWriter;
import com.jvmux.network.http.io.FixedBodyWriter;
import com.jvmux.network.http.constant.HttpHeader;
import com.jvmux.network.http.constant.HttpVersion;
import lombok.Builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Builder
public record HttpResponse(
        int status,
        HttpVersion version,
        Map<String, String> headers,
        InputStream body
) {

    public HttpResponse {
        if (headers == null) {
            headers = new HashMap<>();
        }
    }

    public static HttpResponse empty(int status) {
        var res = builder()
                .version(HttpVersion.HTTP_1_1)
                .status(status)
                .build();

        res.setHeader(HttpHeader.CONTENT_LENGTH, "0");
        return res.applyDefaultFraming();
    }

    public String getHeader(String name) {
        return this.headers.get(name);
    }

    public void setHeader(String header, String value) {
        this.headers.put(header, value);
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

    public static HttpResponse text(int status, String text) {
        return from(status, text, new PlainTextEncoder());
    }

    public static HttpResponse html(int status, String html) {
        return from(status, html, new HtmlEncoder());
    }

    public static HttpResponse json(int status, Object content) {
        return from(status, content, new JsonEncoder());
    }

    private static HttpResponse from(int status, Object content, BodyEncoder encoder) {
        byte[] bytes = encoder.encode(content);
        var res = builder()
                .version(HttpVersion.HTTP_1_1)
                .status(status)
                .body(new ByteArrayInputStream(bytes))
                .build();

        res.setHeader(HttpHeader.CONTENT_LENGTH, String.valueOf(bytes.length));
        res.setHeader(HttpHeader.CONTENT_TYPE, encoder.contentType());
        return res.applyDefaultFraming();
    }

    private HttpResponse applyDefaultFraming() {
        this.setHeader(HttpHeader.HOST, "JVMux");
        return this;
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
