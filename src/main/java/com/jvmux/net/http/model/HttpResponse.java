package com.jvmux.net.http.model;

import com.jvmux.net.http.constant.HttpHeader;
import com.jvmux.net.http.constant.HttpVersion;
import com.jvmux.net.http.io.ResponseEncoder;
import lombok.Builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
        var res = HttpResponse.builder()
                .version(HttpVersion.HTTP_1_1)
                .status(status)
                .build();

        res.setHeader(HttpHeader.CONTENT_LENGTH, "0");
        return res.applyDefaultFraming();
    }

    public static HttpResponse text(int status, String text) {
        var res = HttpResponse.builder()
                .version(HttpVersion.HTTP_1_1)
                .status(status)
                .body(new ByteArrayInputStream(text.getBytes(StandardCharsets.US_ASCII)))
                .build();

        res.setHeader(HttpHeader.CONTENT_LENGTH, String.valueOf(text.length()));
        res.setHeader(HttpHeader.CONTENT_TYPE, "text/plain");
        return res.applyDefaultFraming();
    }

    public String getHeader(String name) {
        return this.headers.get(name);
    }

    public void setHeader(String header, String value) {
        this.headers.put(header, value);
    }

    public byte[] encode() {
        return ResponseEncoder.encode(this);
    }

    private HttpResponse applyDefaultFraming() {
        this.setHeader(HttpHeader.HOST, "JVMux");
        return this;
    }
}
