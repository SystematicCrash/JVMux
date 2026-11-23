package com.jvmux.network.http.constant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HttpMethodTest {

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "PATCH", "OPTIONS", "TRACE", "CONNECT"})
    void shouldRecognizeValidHttpMethods(String method) {
        assertTrue(HttpMethod.isValid(method));
        Optional<HttpMethod> found = HttpMethod.find(method);
        assertTrue(found.isPresent());
        assertEquals(method, found.get().value());
        assertEquals(method, found.get().toString());
        assertEquals(HttpMethod.valueOf(method), HttpMethod.from(method));
    }

    @Test
    void shouldNotRecognizeAnyOverTheWire() {
        assertFalse(HttpMethod.isValid("ANY"));
        assertTrue(HttpMethod.find("ANY").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> HttpMethod.from("ANY"));
    }

    @Test
    void shouldHandleNullAndInvalidMethods() {
        assertFalse(HttpMethod.isValid(null));
        assertFalse(HttpMethod.isValid(""));
        assertFalse(HttpMethod.isValid("INVALID"));
        assertFalse(HttpMethod.isValid("get")); // Case sensitive

        assertTrue(HttpMethod.find(null).isEmpty());
        assertTrue(HttpMethod.find("").isEmpty());
        assertTrue(HttpMethod.find("UNKNOWN").isEmpty());

        assertThrows(IllegalArgumentException.class, () -> HttpMethod.from("FOOBAR"));
    }
}
