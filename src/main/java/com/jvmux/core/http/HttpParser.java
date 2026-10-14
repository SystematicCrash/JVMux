package com.jvmux.core.http;

import com.jvmux.core.http.constant.HttpMethod;
import com.jvmux.core.http.constant.HttpVersion;
import com.jvmux.util.Pair;

import java.util.*;

import static com.jvmux.core.http.constant.HttpConstants.*;

public class HttpParser {

    public static HttpRequest parseRequest(String[] raw) {
        var requestLine = parseRequestLine(raw[0]);
        var headers = parseHeaders(
                Arrays.copyOfRange(raw, 1, raw.length)
        );

        return HttpRequest.builder()
                .path(requestLine.path())
                .method(requestLine.method())
                .version(requestLine.version())
                .headers(headers)
                .build();
    }

    private static RequestLine parseRequestLine(String line) {
        if (line.length() > MAX_REQUEST_LINE_LEN) {
            throw new IllegalArgumentException("Bad request line format");
        }

        var parts = line.split(String.valueOf(SP), -1);

        if (parts.length != 3) {
            throw new IllegalArgumentException("Bad request line format");
        }

        for (String part : parts) {
            if (part.isEmpty()) {
                throw new IllegalArgumentException("Bad request line format");
            }
        }

        var method = parts[0];
        var target = parts[1];
        var version = parts[2];

        if (!VALID_METHODS.contains(method)) {
            throw new IllegalArgumentException("Bad request line format");
        }
        if (!VALID_VERSIONS.contains(version)) {
            throw new IllegalArgumentException("Bad request line format");
        }

        return RequestLine.builder()
                .path(target)
                .method(HttpMethod.from(method))
                .version(HttpVersion.from(version))
                .build();
    }

    private static Map<String, String> parseHeaders(String[] raw) {
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
