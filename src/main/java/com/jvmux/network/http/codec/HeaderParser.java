package com.jvmux.network.http.codec;

import com.jvmux.common.Pair;

import java.util.*;

import static com.jvmux.network.http.constant.Constants.*;
import static com.jvmux.network.http.constant.Constants.COLON;

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
        var parsed = new HashMap<String, String>();

        if (raw.length == 0) {
            throw new IllegalArgumentException("Invalid Headers");
        }

        int totalLength = Arrays.stream(raw).reduce(0,
                (sum, str) -> sum + str.length(), Integer::sum
        );

        if (totalLength > MAX_HEADER_SIZE) {
            throw new IllegalArgumentException("Invalid Headers");
        }
        if (raw.length > MAX_HEADER_COUNT) {
            throw new IllegalArgumentException("Invalid Headers");
        }

        for (var rawHeader : raw) {
            var header = parseHeader(rawHeader);

            if (parsed.containsKey(header.key())) {
                if (UNIQUE_HEADERS.contains(header.key()))
                    throw new IllegalArgumentException("Invalid Headers");
            }

            parsed.put(header.key(), header.value());
        }

        return parsed;
    }

    private static Pair<String, String> parseHeader(String headerLine) {
        var idx = headerLine.indexOf(COLON);
        var name = headerLine.substring(0, idx).trim().toLowerCase();
        var value = headerLine.substring(idx + 1).trim();

        if (name.isEmpty() || value.isEmpty()) {
            throw new IllegalArgumentException("Invalid Headers");
        }

        return new Pair<>(name, value);
    }

    private static void checkRequestHeaders(Set<String> headers) {
        if (headers.containsAll(REQUEST_MANDATORY_HEADERS)) {
            throw new IllegalArgumentException("Invalid Headers");
        }
    }

    private static void checkResponseHeaders(Set<String> headers) {
        var hasMandatory = headers.containsAll(RESPONSE_MANDATORY_HEADERS);
        var hasPayloadSpecifier = !Collections.disjoint(headers, PAYLOAD_SPECIFIER_HEADERS);

        if (!hasMandatory || !hasPayloadSpecifier) {
            throw new IllegalArgumentException("Invalid Headers");
        }
    }
}
