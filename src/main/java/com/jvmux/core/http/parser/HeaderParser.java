package com.jvmux.core.http.parser;

import com.jvmux.util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.jvmux.core.http.constant.Constants.*;

public class HeaderParser {
    public static Map<String, String> parseHeaders(String[] raw) {
        var parsed = new HashMap<String, String>();

        if (raw.length == 0) {
            throw new IllegalArgumentException("Bad headers format");
        }

        int totalLength = Arrays.stream(raw).reduce(0,
                (sum, str) -> sum + str.length(), Integer::sum
        );

        if (totalLength > MAX_HEADER_SIZE) {
            throw new IllegalArgumentException("Bad headers format");
        }
        if (raw.length > MAX_HEADER_COUNT) {
            throw new IllegalArgumentException("Bad headers format");
        }

        for (var rawHeader : raw) {
            var header = parseHeader(rawHeader);

            if (parsed.containsKey(header.key())) {
                if (UNIQUE_HEADERS.contains(header.key()))
                    throw new IllegalArgumentException("Bad headers format");
            }

            parsed.put(header.key(), header.value());
        }

        if (!parsed.keySet().containsAll(MANDATORY_HEADERS)) {
            throw new IllegalArgumentException("Bad headers format");
        }

        return parsed;
    }

    private static Pair<String, String> parseHeader(String headerLine) {
        var idx = headerLine.indexOf(COLON);
        var name = headerLine.substring(0, idx);
        var value = headerLine.substring(idx + 1);

        if (name.isEmpty() || value.isEmpty()) {
            throw new IllegalArgumentException("Bad headers format");
        }

        return new Pair<>(name.trim(), value.trim());
    }

}
