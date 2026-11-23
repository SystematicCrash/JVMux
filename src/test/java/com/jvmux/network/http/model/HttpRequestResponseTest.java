package com.jvmux.network.http.model;

import com.jvmux.network.http.constant.HttpHeader;
import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestResponseTest {

    @Test
    void shouldCreateHttpRequestWithDefaultFraming() {
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/test")
                .headers(new HashMap<>())
                .build();

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/test", request.getPath());
        assertEquals(HttpVersion.HTTP_1_1, request.getVersion());
        assertTrue(request.hasHeader(HttpHeader.VIA));
        assertTrue(request.getHeader(HttpHeader.VIA).contains("JVMux"));
        assertNotNull(request.getBody());
    }

    @Test
    void shouldAppendToExistingViaHeaderInRequest() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeader.VIA, "1.0 custom-proxy");

        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("/api")
                .headers(headers)
                .build();

        String via = request.getHeader(HttpHeader.VIA);
        assertTrue(via.startsWith("1.0 custom-proxy, HTTP/1.1 JVMux"));
    }

    @Test
    void shouldDetectCloseConnectionInRequest() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeader.CONNECTION, "close");

        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/test")
                .headers(headers)
                .build();

        assertTrue(request.isCloseConnection());
    }

    @Test
    void shouldCreateEmptyHttpResponse() {
        HttpResponse response = HttpResponse.empty(204);

        assertEquals(204, response.getStatus());
        assertEquals("0", response.getHeader(HttpHeader.CONTENT_LENGTH));
        assertEquals("JVMux", response.getHeader(HttpHeader.SERVER));
        assertEquals("keep-alive", response.getHeader(HttpHeader.CONNECTION));
        assertTrue(response.hasHeader(HttpHeader.DATE));
        assertTrue(response.hasHeader(HttpHeader.KEEP_ALIVE));
        assertTrue(response.isKeepAlive());
        assertFalse(response.isCloseConnection());
    }

    @Test
    void shouldSetAsLastOnHttpResponse() {
        HttpResponse response = HttpResponse.empty(200);
        response.setAsLast();

        assertEquals("close", response.getHeader(HttpHeader.CONNECTION));
        assertTrue(response.isCloseConnection());
        assertFalse(response.isKeepAlive());
    }

    @Test
    void shouldCreateTextHtmlJsonResponses() {
        HttpResponse text = HttpResponse.text(200, "hello");
        assertEquals("text/plain", text.getHeader(HttpHeader.CONTENT_TYPE));
        assertEquals("5", text.getHeader(HttpHeader.CONTENT_LENGTH));

        HttpResponse html = HttpResponse.html(200, "<b>hello</b>");
        assertEquals("text/html", html.getHeader(HttpHeader.CONTENT_TYPE));
        assertEquals("12", html.getHeader(HttpHeader.CONTENT_LENGTH));

        HttpResponse json = HttpResponse.json(200, Map.of("key", "value"));
        assertEquals("application/json", json.getHeader(HttpHeader.CONTENT_TYPE));
        assertTrue(Integer.parseInt(json.getHeader(HttpHeader.CONTENT_LENGTH)) > 0);
    }

    @Test
    void shouldObtainBodyWriterFromRequestAndResponse() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeader.CONTENT_LENGTH, "5");
        InputStream in = new ByteArrayInputStream("hello".getBytes(StandardCharsets.US_ASCII));

        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("/submit")
                .headers(headers)
                .body(in)
                .build();

        assertTrue(request.getWriter().isPresent());

        HttpResponse response = HttpResponse.builder()
                .status(200)
                .headers(headers)
                .body(in)
                .build();

        assertTrue(response.getWriter().isPresent());
    }
}
