package com.jvmux.network.http.constant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class HttpStatusTest {

    @Test
    void shouldValidateStatusCodeRanges() {
        assertTrue(HttpStatus.isValid(100));
        assertTrue(HttpStatus.isValid(200));
        assertTrue(HttpStatus.isValid(404));
        assertTrue(HttpStatus.isValid(502));
        assertTrue(HttpStatus.isValid(599));

        assertFalse(HttpStatus.isValid(99));
        assertFalse(HttpStatus.isValid(600));
        assertFalse(HttpStatus.isValid(-1));
    }

    @ParameterizedTest
    @CsvSource({
            "100, Continue",
            "101, Switching Protocols",
            "200, OK",
            "201, Created",
            "204, No Content",
            "301, Moved Permanently",
            "302, Found",
            "304, Not Modified",
            "400, Bad Request",
            "401, Unauthorized",
            "403, Forbidden",
            "404, Not Found",
            "405, Method Not Allowed",
            "408, Request Timeout",
            "413, Content Too Large",
            "429, Too Many Requests",
            "431, Request Header Fields Too Large",
            "500, Internal Server Error",
            "501, Not Implemented",
            "502, Bad Gateway",
            "503, Service Unavailable",
            "504, Gateway Timeout",
            "505, HTTP Version Not Supported"
    })
    void shouldReturnCorrectReasonPhrase(int code, String expectedReason) {
        assertEquals(expectedReason, HttpStatus.reasonPhrase(code));
    }

    @Test
    void shouldReturnUnknownStatusForUnmappedCode() {
        assertEquals("Unknown Status", HttpStatus.reasonPhrase(299));
        assertEquals("Unknown Status", HttpStatus.reasonPhrase(999));
    }
}
