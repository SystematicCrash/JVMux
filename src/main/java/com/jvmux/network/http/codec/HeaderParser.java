package com.jvmux.network.http.codec;

import com.jvmux.common.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.jvmux.network.http.constant.Constants.*;
import static com.jvmux.network.http.constant.Constants.COLON;
import static com.jvmux.network.http.constant.Constants.MANDATORY_HEADERS;

public class HeaderParser {

    public static Map<String, String> parseHeaders(String[] raw) {
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

        if (!parsed.keySet().containsAll(MANDATORY_HEADERS)) {
            throw new IllegalArgumentException("Invalid Headers");
        }

        return parsed;
    }

    private static Pair<String, String> parseHeader(String headerLine) {
        var idx = headerLine.indexOf(COLON);
        var name = headerLine.substring(0, idx);
        var value = headerLine.substring(idx + 1);

        if (name.isEmpty() || value.isEmpty()) {
            throw new IllegalArgumentException("Invalid Headers");
        }

        return new Pair<>(name.trim(), value.trim());
    }
}
