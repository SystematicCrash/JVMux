package com.jvmux.network.http.model;

import com.jvmux.network.http.body.BodyWriter;
import com.jvmux.network.http.body.BodyWriterFactory;
import com.jvmux.network.http.codec.payload.HtmlEncoder;
import com.jvmux.network.http.codec.payload.JsonEncoder;
import com.jvmux.network.http.codec.payload.PayloadEncoder;
import com.jvmux.network.http.codec.payload.PlainTextEncoder;
import com.jvmux.network.http.constant.HttpHeader;
import com.jvmux.network.http.constant.HttpVersion;
import lombok.Builder;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class HttpResponse {

    private final int status;
    private final HttpVersion version;
    private final Map<String, String> headers;
    private final InputStream body;

    @Builder
    public HttpResponse(int status, HttpVersion version, Map<String, String> headers, InputStream body) {
        this.status = status;
        this.version = version != null ? version : HttpVersion.HTTP_1_1;
        this.headers = new HashMap<>(headers);
        this.body = body != null ? body : InputStream.nullInputStream();
        this.applyDefaultFraming();
    }

    public static HttpResponse empty(int status) {
        HttpResponse res = builder()
                .version(HttpVersion.HTTP_1_1)
                .status(status)
                .headers(new HashMap<>())
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

    public void setHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public HttpResponse setAsLast() {
        setHeader(HttpHeader.CONNECTION, "close");
        return this;
    }

    public boolean isCloseConnection() {
        String connection = getHeader(HttpHeader.CONNECTION);
        if (connection == null) {
            return this.version == HttpVersion.HTTP_1_0;
        }
        return "close".equalsIgnoreCase(connection);
    }

    public boolean isKeepAlive() {
        return !isCloseConnection();
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
        HttpResponse res = builder()
                .version(HttpVersion.HTTP_1_1)
                .status(status)
                .headers(new HashMap<>())
                .body(new ByteArrayInputStream(bytes))
                .build();

        res.setHeader(HttpHeader.CONTENT_LENGTH, String.valueOf(bytes.length));
        res.setHeader(HttpHeader.CONTENT_TYPE, encoder.contentType());
        return res;
    }

    private void applyDefaultFraming() {
        if (!hasHeader(HttpHeader.SERVER)) {
            setHeader(HttpHeader.SERVER, "JVMux");
        }

        if (!hasHeader(HttpHeader.DATE)) {
            setHeader(
                    HttpHeader.DATE,
                    DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC))
            );
        }
    }
}