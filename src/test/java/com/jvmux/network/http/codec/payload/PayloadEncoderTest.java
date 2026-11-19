package com.jvmux.network.http.codec.payload;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PayloadEncoderTest {

    @Test
    void shouldEncodePlainTextProperly() {
        PlainTextEncoder encoder = new PlainTextEncoder();
        assertEquals("text/plain", encoder.contentType());

        byte[] encoded = encoder.encode("Hello World");
        assertEquals("Hello World", new String(encoded, StandardCharsets.US_ASCII));

        byte[] numberEncoded = encoder.encode(12345);
        assertEquals("12345", new String(numberEncoded, StandardCharsets.US_ASCII));
    }

    @Test
    void shouldEncodeHtmlProperly() {
        HtmlEncoder encoder = new HtmlEncoder();
        assertEquals("text/html", encoder.contentType());

        byte[] encoded = encoder.encode("<h1>Title</h1>");
        assertEquals("<h1>Title</h1>", new String(encoded, StandardCharsets.US_ASCII));
    }

    @Test
    void shouldEncodeJsonPrimitivesProperly() {
        JsonEncoder encoder = new JsonEncoder();
        assertEquals("application/json", encoder.contentType());

        assertEquals("null", new String(encoder.encode(null), StandardCharsets.US_ASCII));
        assertEquals("123", new String(encoder.encode(123), StandardCharsets.US_ASCII));
        assertEquals("3.14", new String(encoder.encode(3.14), StandardCharsets.US_ASCII));
        assertEquals("true", new String(encoder.encode(true), StandardCharsets.US_ASCII));
        assertEquals("\"hello\"", new String(encoder.encode("hello"), StandardCharsets.US_ASCII));
    }

    @Test
    void shouldEscapeJsonStringsProperly() {
        JsonEncoder encoder = new JsonEncoder();
        String json = new String(encoder.encode("Hello \"World\"\nNew\tTab\\Backslash"), StandardCharsets.US_ASCII);
        assertEquals("\"Hello \\\"World\\\"\\nNew\\tTab\\\\Backslash\"", json);
    }

    @Test
    void shouldEncodeJsonCollectionsAndMaps() {
        JsonEncoder encoder = new JsonEncoder();

        List<Object> list = List.of("item1", 42, false);
        assertEquals("[\"item1\",42,false]", new String(encoder.encode(list), StandardCharsets.US_ASCII));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("key", "value");
        map.put("count", 10);
        assertEquals("{\"key\":\"value\",\"count\":10}", new String(encoder.encode(map), StandardCharsets.US_ASCII));
    }
}
