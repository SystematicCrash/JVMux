package com.jvmux.network.http.codec.decode;

import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RequestResponseParserTest {

    @Test
    void shouldParseValidHttpRequest() {
        String[] raw = {
                "GET /index.html HTTP/1.1",
                "Host: localhost:8080",
                "User-Agent: curl/7.68.0"
        };
        InputStream body = new ByteArrayInputStream("test body".getBytes(StandardCharsets.US_ASCII));

        HttpRequest request = RequestParser.parse(raw, body);
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/index.html", request.getPath());
        assertEquals(HttpVersion.HTTP_1_1, request.getVersion());
        assertEquals("localhost:8080", request.getHeader("host"));
        assertEquals("curl/7.68.0", request.getHeader("user-agent"));
        assertSame(body, request.getBody());
    }

    @Test
    void shouldThrowOnInvalidRequestLine() {
        assertThrows(IllegalArgumentException.class,
                () -> RequestParser.parse(null, InputStream.nullInputStream()));
        assertThrows(IllegalArgumentException.class,
                () -> RequestParser.parse(new String[0], InputStream.nullInputStream()));
        assertThrows(IllegalArgumentException.class,
                () -> RequestParser.parse(new String[]{""}, InputStream.nullInputStream()));
        assertThrows(IllegalArgumentException.class,
                () -> RequestParser.parse(new String[]{"GET /onlytwo"}, InputStream.nullInputStream()));
        assertThrows(IllegalArgumentException.class,
                () -> RequestParser.parse(new String[]{"INVALID /path HTTP/1.1", "Host: localhost"}, InputStream.nullInputStream()));
        assertThrows(IllegalArgumentException.class,
                () -> RequestParser.parse(new String[]{"GET /path HTTP/1.0", "Host: localhost"}, InputStream.nullInputStream()));
    }

    @Test
    void shouldParseValidHttpResponse() {
        String[] raw = {
                "HTTP/1.1 200 OK",
                "Content-Type: text/plain",
                "Content-Length: 12"
        };
        InputStream body = new ByteArrayInputStream("hello world!".getBytes(StandardCharsets.US_ASCII));

        HttpResponse response = ResponseParser.parse(raw, body);
        assertEquals(HttpVersion.HTTP_1_1, response.getVersion());
        assertEquals(200, response.getStatus());
        assertEquals("text/plain", response.getHeader("content-type"));
        assertEquals("12", response.getHeader("content-length"));
        assertSame(body, response.getBody());
    }

    @Test
    void shouldParseResponseWithoutReasonPhrase() {
        String[] raw = {
                "HTTP/1.1 204",
                "Content-Length: 0"
        };

        HttpResponse response = ResponseParser.parse(raw, InputStream.nullInputStream());
        assertEquals(HttpVersion.HTTP_1_1, response.getVersion());
        assertEquals(204, response.getStatus());
    }

    @Test
    void shouldThrowOnInvalidResponseLine() {
        assertThrows(IllegalArgumentException.class,
                () -> ResponseParser.parse(null, InputStream.nullInputStream()));
        assertThrows(IllegalArgumentException.class,
                () -> ResponseParser.parse(new String[0], InputStream.nullInputStream()));
        assertThrows(IllegalArgumentException.class,
                () -> ResponseParser.parse(new String[]{""}, InputStream.nullInputStream()));
        assertThrows(IllegalArgumentException.class,
                () -> ResponseParser.parse(new String[]{"HTTP/1.1"}, InputStream.nullInputStream()));
        assertThrows(IllegalArgumentException.class,
                () -> ResponseParser.parse(new String[]{"HTTP/1.1 NOT_AN_INT OK"}, InputStream.nullInputStream()));
        assertThrows(IllegalArgumentException.class,
                () -> ResponseParser.parse(new String[]{"INVALID/1.1 200 OK"}, InputStream.nullInputStream()));
    }
}
