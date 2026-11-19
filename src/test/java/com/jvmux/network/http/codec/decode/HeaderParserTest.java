package com.jvmux.network.http.codec.decode;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HeaderParserTest {

    @Test
    void shouldParseValidRequestHeaders() {
        String[] raw = {
                "Host: example.com",
                "User-Agent: test-agent",
                "Accept: text/html",
                "Accept: application/json"
        };

        Map<String, String> headers = HeaderParser.parseRequestHeaders(raw);
        assertEquals("example.com", headers.get("host"));
        assertEquals("test-agent", headers.get("user-agent"));
        assertEquals("text/html, application/json", headers.get("accept"));
    }

    @Test
    void shouldParseValidResponseHeaders() {
        String[] raw = {
                "Content-Type: text/plain",
                "Content-Length: 42"
        };

        Map<String, String> headers = HeaderParser.parseResponseHeaders(raw);
        assertEquals("text/plain", headers.get("content-type"));
        assertEquals("42", headers.get("content-length"));
    }

    @Test
    void shouldThrowWhenEmpty() {
        assertThrows(IllegalArgumentException.class, () -> HeaderParser.parseRequestHeaders(null));
        assertThrows(IllegalArgumentException.class, () -> HeaderParser.parseRequestHeaders(new String[0]));
    }

    @Test
    void shouldThrowWhenMandatoryRequestHeaderHostIsMissing() {
        String[] raw = {
                "User-Agent: test-agent"
        };

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> HeaderParser.parseRequestHeaders(raw));
        assertTrue(ex.getMessage().contains("Missing mandatory request header"));
    }

    @Test
    void shouldThrowOnDuplicateSingletonHeader() {
        String[] raw = {
                "Host: example.com",
                "Host: another.com"
        };

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> HeaderParser.parseRequestHeaders(raw));
        assertTrue(ex.getMessage().contains("Duplicate singleton header 'host'"));
    }

    @Test
    void shouldThrowOnConflictingFramingHeaders() {
        String[] raw = {
                "Host: example.com",
                "Content-Length: 10",
                "Transfer-Encoding: chunked"
        };

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> HeaderParser.parseRequestHeaders(raw));
        assertTrue(ex.getMessage().contains("Conflicting framing headers"));
    }

    @Test
    void shouldThrowOnIllegalWhitespaceBeforeColon() {
        String[] raw = {
                "Host : example.com"
        };

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> HeaderParser.parseRequestHeaders(raw));
        assertTrue(ex.getMessage().contains("Illegal whitespace detected before colon"));
    }

    @Test
    void shouldThrowOnMissingColonOrEmptyName() {
        assertThrows(IllegalArgumentException.class,
                () -> HeaderParser.parseRequestHeaders(new String[]{"NoColonLine"}));
        assertThrows(IllegalArgumentException.class,
                () -> HeaderParser.parseRequestHeaders(new String[]{": value"}));
    }
}
