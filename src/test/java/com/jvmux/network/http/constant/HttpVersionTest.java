package com.jvmux.network.http.constant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HttpVersionTest {

    @ParameterizedTest
    @ValueSource(strings = {"HTTP/1.0", "HTTP/1.1", "HTTP/2", "HTTP/3"})
    void shouldRecognizeValidHttpVersions(String raw) {
        assertTrue(HttpVersion.isValid(raw));
        Optional<HttpVersion> found = HttpVersion.find(raw);
        assertTrue(found.isPresent());
        assertEquals(raw, found.get().value());
        assertEquals(raw, found.get().toString());
        assertEquals(raw, HttpVersion.from(raw).value());
    }

    @Test
    void shouldHandleNullAndInvalidVersions() {
        assertFalse(HttpVersion.isValid(null));
        assertFalse(HttpVersion.isValid(""));
        assertFalse(HttpVersion.isValid("HTTP/0.9"));
        assertFalse(HttpVersion.isValid("http/1.1"));

        assertTrue(HttpVersion.find(null).isEmpty());
        assertTrue(HttpVersion.find("").isEmpty());
        assertTrue(HttpVersion.find("HTTP/4.0").isEmpty());

        assertThrows(IllegalArgumentException.class, () -> HttpVersion.from("HTTP/9.9"));
    }
}
