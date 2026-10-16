package com.jvmux.core.http.parser;

import com.jvmux.core.http.error.HttpException;
import com.jvmux.util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.jvmux.core.http.constant.Constants.*;

public class HeaderParser {
    public static Map<String, String> parse(String[] raw) {
        var parsed = new HashMap<String, String>();

        if (raw.length == 0) {
            throw HttpException.badRequest("Bad headers");
        }

        int totalLength = Arrays.stream(raw).reduce(0,
                (sum, str) -> sum + str.length(), Integer::sum
        );

        if (totalLength > MAX_HEADER_SIZE) {
            throw HttpException.badRequest("Bad headers");
        }
        if (raw.length > MAX_HEADER_COUNT) {
            throw HttpException.badRequest("Bad headers");
        }

        for (var rawHeader : raw) {
            var header = parseHeader(rawHeader);

            if (parsed.containsKey(header.key())) {
                if (UNIQUE_HEADERS.contains(header.key()))
                    throw HttpException.badRequest("Bad headers");
            }

            parsed.put(header.key(), header.value());
        }

        if (!parsed.keySet().containsAll(MANDATORY_HEADERS)) {
            throw HttpException.badRequest("Bad headers");
        }

        return parsed;
    }

    private static Pair<String, String> parseHeader(String headerLine) {
        var idx = headerLine.indexOf(COLON);
        var name = headerLine.substring(0, idx);
        var value = headerLine.substring(idx + 1);

        if (name.isEmpty() || value.isEmpty()) {
            throw HttpException.badRequest("Bad headers");
        }

        return new Pair<>(name.trim(), value.trim());
    }

}
