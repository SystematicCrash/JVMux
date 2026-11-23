package com.jvmux.network.http.io;

import com.jvmux.network.http.exception.HttpException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HeaderReaderTest {

    @Test
    void shouldReadHeadersSuccessfully() throws IOException {
        String data = "GET /index.html HTTP/1.1\r\nHost: localhost\r\nAccept: */*\r\n\r\n";
        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes(StandardCharsets.US_ASCII));

        String[] headers = HeaderReader.readHeaders(in, false);
        assertNotNull(headers);
        assertEquals(3, headers.length);
        assertEquals("GET /index.html HTTP/1.1", headers[0]);
        assertEquals("Host: localhost", headers[1]);
        assertEquals("Accept: */*", headers[2]);
    }

    @Test
    void shouldReturnNullOnImmediateEof() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        String[] headers = HeaderReader.readHeaders(in, false);
        assertNull(headers);
    }

    @Test
    void shouldThrowOnIncompleteHeadersBeforeEof() {
        String data = "GET / HTTP/1.1\r\nHost: loc";
        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes(StandardCharsets.US_ASCII));

        // Downstream client EOF
        assertThrows(EOFException.class, () -> HeaderReader.readHeaders(in, false));

        // Upstream backend EOF
        ByteArrayInputStream inUpstream = new ByteArrayInputStream(data.getBytes(StandardCharsets.US_ASCII));
        HttpException ex = assertThrows(HttpException.class, () -> HeaderReader.readHeaders(inUpstream, true));
        assertEquals(502, ex.getStatus());
    }

    @Test
    void shouldThrowWhenHeadersExceedMaxSize() {
        // Create headers > 8192 bytes
        StringBuilder sb = new StringBuilder("GET / HTTP/1.1\r\n");
        while (sb.length() < 9000) {
            sb.append("X-Large-Header: 12345678901234567890\r\n");
        }
        sb.append("\r\n");

        ByteArrayInputStream inDownstream = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.US_ASCII));
        HttpException downstreamEx = assertThrows(HttpException.class, () -> HeaderReader.readHeaders(inDownstream, false));
        assertEquals(431, downstreamEx.getStatus());

        ByteArrayInputStream inUpstream = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.US_ASCII));
        HttpException upstreamEx = assertThrows(HttpException.class, () -> HeaderReader.readHeaders(inUpstream, true));
        assertEquals(502, upstreamEx.getStatus());
    }
}
