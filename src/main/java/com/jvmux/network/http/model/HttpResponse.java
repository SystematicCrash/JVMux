package com.jvmux.network.http.model;

import com.jvmux.network.http.codec.payload.PayloadEncoder;
import com.jvmux.network.http.codec.payload.HtmlEncoder;
import com.jvmux.network.http.codec.payload.JsonEncoder;
import com.jvmux.network.http.codec.payload.PlainTextEncoder;
import com.jvmux.network.http.body.BodyWriter;
import com.jvmux.network.http.body.BodyWriterFactory;
import com.jvmux.network.http.constant.HttpHeader;
import com.jvmux.network.http.constant.HttpVersion;
import lombok.Builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        this.applyDefaultFraming();
    }

    public static HttpResponse empty(int status) {
        var res = builder()
                .version(HttpVersion.HTTP_1_1)
                .status(status)
                .build();

        res.setHeader(HttpHeader.CONTENT_LENGTH, "0");
        return res;
    }

    public String getHeader(String name) {
        return this.headers.get(name);
    }

    public boolean hasHeader(String name) {
        return this.headers.containsKey(name);
    }

    public void setHeader(String header, String value) {
        this.headers.put(header, value);
    }

    public Optional<BodyWriter> getWriter() {
        return BodyWriterFactory.create(this.body, this.headers);
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

    private static HttpResponse from(int status, Object content, PayloadEncoder encoder) {
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
        setHeader(HttpHeader.HOST, "JVMux");

        if (!hasHeader(HttpHeader.DATE))
            setHeader(
                    HttpHeader.DATE,
                    DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC))
            );

        return this;
    }
}
