package com.jvmux.network.http.codec.decode;

import com.jvmux.common.Pair;
import com.jvmux.network.http.constant.HttpHeader;

import java.util.*;

import static com.jvmux.network.http.constant.Constants.*;

public class HeaderParser {

    public static Map<String, String> parseRequestHeaders(String[] raw) {
        var headers = parseHeaders(raw);
        checkRequestHeaders(headers.keySet());
        return headers;
    }

    public static Map<String, String> parseResponseHeaders(String[] raw) {
        var headers = parseHeaders(raw);
        checkResponseHeaders(headers.keySet());
        return headers;
    }

    private static Map<String, String> parseHeaders(String[] raw) {
        if (raw == null || raw.length == 0) {
            throw new IllegalArgumentException("Header section cannot be empty");
        }

        if (raw.length > MAX_HEADER_COUNT) {
            throw new IllegalArgumentException(String.format(
                    "Header count (%d) exceeds maximum allowed limit (%d)",
                    raw.length, MAX_HEADER_COUNT
            ));
        }

        int totalLength = Arrays.stream(raw).mapToInt(String::length).sum();
        if (totalLength > MAX_HEADER_SIZE) {
            throw new IllegalArgumentException(String.format(
                    "Total header size (%d bytes) exceeds maximum allowed limit (%d bytes)",
                    totalLength, MAX_HEADER_SIZE
            ));
        }

        var parsed = new HashMap<String, String>();

        for (var rawHeader : raw) {
            var header = parseHeader(rawHeader);

            if (parsed.containsKey(header.key())) {
                if (UNIQUE_HEADERS.contains(header.key())) {
                    throw new IllegalArgumentException(String.format(
                            "Duplicate singleton header '%s' detected (duplicate values: '%s', '%s')",
                            header.key(), parsed.get(header.key()), header.value()
                    ));
                }
                parsed.put(header.key(), parsed.get(header.key()) + ", " + header.value());
            } else {
                parsed.put(header.key(), header.value());
            }
        }

        return parsed;
    }

    private static Pair<String, String> parseHeader(String headerLine) {
        if (headerLine == null || headerLine.isEmpty()) {
            throw new IllegalArgumentException("Malformed header: empty line encountered within header block");
        }

        var idx = headerLine.indexOf(COLON);
        if (idx <= 0) {
            throw new IllegalArgumentException(String.format(
                    "Malformed header line: missing colon or missing header name in '%s'",
                    headerLine
            ));
        }

        if (Character.isWhitespace(headerLine.charAt(idx - 1))) {
            throw new IllegalArgumentException(String.format(
                    "Illegal whitespace detected before colon in header '%s'",
                    headerLine
            ));
        }

        var name = headerLine.substring(0, idx).trim().toLowerCase(Locale.ROOT);
        var value = headerLine.substring(idx + 1).trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "Header field-name cannot be empty in '%s'",
                    headerLine
            ));
        }

        return new Pair<>(name, value);
    }

    private static void checkRequestHeaders(Set<String> headers) {
        if (!headers.containsAll(REQUEST_MANDATORY_HEADERS)) {
            var missing = new HashSet<>(REQUEST_MANDATORY_HEADERS);
            missing.removeAll(headers);
            throw new IllegalArgumentException(String.format(
                    "Missing mandatory request header(s): %s",
                    missing
            ));
        }

        if (headers.contains(HttpHeader.CONTENT_LENGTH) && headers.contains(HttpHeader.TRANSFER_ENCODING)) {
            throw new IllegalArgumentException(
                    "Conflicting framing headers: request contains both 'content-length' and 'transfer-encoding'"
            );
        }
    }

    private static void checkResponseHeaders(Set<String> headers) {
        if (headers.contains(HttpHeader.CONTENT_LENGTH) && headers.contains(HttpHeader.TRANSFER_ENCODING)) {
            throw new IllegalArgumentException(
                    "Conflicting framing headers: response contains both 'content-length' and 'transfer-encoding'"
            );
        }
    }
}