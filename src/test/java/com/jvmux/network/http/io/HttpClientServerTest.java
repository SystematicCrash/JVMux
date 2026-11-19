package com.jvmux.network.http.io;

import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.exception.HttpException;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpClientServerTest {

    @Test
    void shouldWriteAndReadHttpRequestBetweenClientAndServer() throws IOException {
        String bodyText = "Request payload data";
        byte[] bodyBytes = bodyText.getBytes(StandardCharsets.US_ASCII);

        Map<String, String> headers = new HashMap<>();
        headers.put("host", "backend.local");
        headers.put("content-length", String.valueOf(bodyBytes.length));

        HttpRequest originalRequest = HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("/submit")
                .version(HttpVersion.HTTP_1_1)
                .headers(headers)
                .body(new ByteArrayInputStream(bodyBytes))
                .build();

        // Client writes request to stream
        ByteArrayOutputStream pipe = new ByteArrayOutputStream();
        HttpClient.writeRequest(originalRequest, pipe);

        // Server reads request from stream
        ByteArrayInputStream serverIn = new ByteArrayInputStream(pipe.toByteArray());
        HttpRequest receivedRequest = HttpServer.readRequest(serverIn);

        assertNotNull(receivedRequest);
        assertEquals(HttpMethod.POST, receivedRequest.getMethod());
        assertEquals("/submit", receivedRequest.getPath());
        assertEquals(HttpVersion.HTTP_1_1, receivedRequest.getVersion());
        assertEquals("backend.local", receivedRequest.getHeader("host"));
        assertEquals(String.valueOf(bodyBytes.length), receivedRequest.getHeader("content-length"));

        byte[] readBody = receivedRequest.getBody().readNBytes(bodyBytes.length);
        assertEquals(bodyText, new String(readBody, StandardCharsets.US_ASCII));
    }

    @Test
    void shouldWriteAndReadHttpResponseBetweenServerAndClient() throws IOException {
        String bodyText = "Response message payload";
        byte[] bodyBytes = bodyText.getBytes(StandardCharsets.US_ASCII);

        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "text/plain");
        headers.put("content-length", String.valueOf(bodyBytes.length));

        HttpResponse originalResponse = HttpResponse.builder()
                .status(200)
                .version(HttpVersion.HTTP_1_1)
                .headers(headers)
                .body(new ByteArrayInputStream(bodyBytes))
                .build();

        // Server writes response to stream
        ByteArrayOutputStream pipe = new ByteArrayOutputStream();
        HttpServer.writeResponse(originalResponse, pipe);

        // Client reads response from stream
        ByteArrayInputStream clientIn = new ByteArrayInputStream(pipe.toByteArray());
        HttpResponse receivedResponse = HttpClient.readResponse(clientIn);

        assertNotNull(receivedResponse);
        assertEquals(200, receivedResponse.getStatus());
        assertEquals(HttpVersion.HTTP_1_1, receivedResponse.getVersion());
        assertEquals("text/plain", receivedResponse.getHeader("content-type"));
        assertEquals(String.valueOf(bodyBytes.length), receivedResponse.getHeader("content-length"));

        byte[] readBody = receivedResponse.getBody().readNBytes(bodyBytes.length);
        assertEquals(bodyText, new String(readBody, StandardCharsets.US_ASCII));
    }

    @Test
    void shouldThrowBadRequestOnMalformedClientRequest() {
        String malformed = "INVALID_LINE\r\n\r\n";
        ByteArrayInputStream in = new ByteArrayInputStream(malformed.getBytes(StandardCharsets.US_ASCII));

        HttpException ex = assertThrows(HttpException.class, () -> HttpServer.readRequest(in));
        assertEquals(400, ex.getStatus());
    }

    @Test
    void shouldThrowBadGatewayOnMalformedUpstreamResponse() {
        String malformed = "INVALID_RESPONSE_LINE\r\n\r\n";
        ByteArrayInputStream in = new ByteArrayInputStream(malformed.getBytes(StandardCharsets.US_ASCII));

        HttpException ex = assertThrows(HttpException.class, () -> HttpClient.readResponse(in));
        assertEquals(502, ex.getStatus());
    }

    @Test
    void shouldReturnNullWhenStreamIsEmpty() throws IOException {
        ByteArrayInputStream empty = new ByteArrayInputStream(new byte[0]);
        assertNull(HttpServer.readRequest(empty));
        assertNull(HttpClient.readResponse(empty));
    }
}
