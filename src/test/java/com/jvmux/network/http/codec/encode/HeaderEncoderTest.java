package com.jvmux.network.http.codec.encode;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HeaderEncoderTest {

    @Test
    void shouldFormatLocationProperly() {
        Optional<String> replaced = HeaderEncoder.formatLocation("http://old.example.com/path", "old.example.com", "new.example.com");
        assertTrue(replaced.isPresent());
        assertEquals("http://new.example.com/path", replaced.get());

        Optional<String> untouched = HeaderEncoder.formatLocation("http://other.com/path", "old.example.com", "new.example.com");
        assertTrue(untouched.isPresent());
        assertEquals("http://other.com/path", untouched.get());

        assertTrue(HeaderEncoder.formatLocation(null, "old", "new").isEmpty());
        assertTrue(HeaderEncoder.formatLocation("loc", null, "new").isEmpty());
        assertTrue(HeaderEncoder.formatLocation("loc", "old", null).isEmpty());
    }

    @Test
    void shouldFormatForwardedForProperly() {
        assertEquals("192.168.1.1", HeaderEncoder.formatForwardedFor(null, "192.168.1.1"));
        assertEquals("10.0.0.1, 192.168.1.1", HeaderEncoder.formatForwardedFor("10.0.0.1", "192.168.1.1"));
    }

    @Test
    void shouldFormatDateProperly() {
        String date = HeaderEncoder.formatDate();
        assertNotNull(date);
        assertTrue(date.endsWith("GMT"));
    }

    @Test
    void shouldFormatKeepAliveProperly() {
        String keepAlive = HeaderEncoder.formatKeepAlive();
        assertNotNull(keepAlive);
        assertTrue(keepAlive.contains("timeout="));
        assertTrue(keepAlive.contains("max="));
    }

    @Test
    void shouldFormatViaProperly() {
        assertEquals("HTTP/1.1 JVMux", HeaderEncoder.formatVia(null));
        assertEquals("HTTP/1.1 JVMux", HeaderEncoder.formatVia(""));
        assertEquals("1.0 proxy, HTTP/1.1 JVMux", HeaderEncoder.formatVia("1.0 proxy"));
    }
}
