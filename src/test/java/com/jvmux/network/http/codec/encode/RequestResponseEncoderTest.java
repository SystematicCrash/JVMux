package com.jvmux.network.http.codec.encode;

import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestResponseEncoderTest {

    @Test
    void shouldEncodeHttpRequestProperly() {
        Map<String, String> headers = new HashMap<>();
        headers.put("host", "example.com");
        headers.put("accept", "*/*");

        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/test")
                .version(HttpVersion.HTTP_1_1)
                .headers(headers)
                .build();

        byte[] encoded = RequestEncoder.encode(request);
        String text = new String(encoded, StandardCharsets.US_ASCII);

        assertTrue(text.startsWith("GET /api/test HTTP/1.1\r\n"));
        assertTrue(text.contains("host: example.com\r\n"));
        assertTrue(text.contains("accept: */*\r\n"));
        assertTrue(text.endsWith("\r\n\r\n"));
    }

    @Test
    void shouldEncodeHttpResponseProperly() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");

        HttpResponse response = HttpResponse.builder()
                .version(HttpVersion.HTTP_1_1)
                .status(200)
                .headers(headers)
                .build();

        byte[] encoded = ResponseEncoder.encode(response);
        String text = new String(encoded, StandardCharsets.US_ASCII);

        assertTrue(text.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(text.contains("content-type: application/json\r\n"));
        assertTrue(text.contains("server: JVMux\r\n"));
        assertTrue(text.endsWith("\r\n\r\n"));
    }
}
