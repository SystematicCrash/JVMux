package com.jvmux.network.http.codec;

import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.exception.HttpException;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.common.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.jvmux.network.http.constant.Constants.*;

public class RequestParser {

    public static HttpRequest parse(String[] raw) {
        return parse(raw, null);
    }

    public static HttpRequest parse(String[] raw, java.io.InputStream body) {
        if (raw.length == 0) {
            throw HttpException.badRequest("Bad Request");
        }
        var parts = parseRequestLine(raw[0]);
        return HttpRequest.builder()
                .method(HttpMethod.from(parts[0]))
                .path(parts[1])
                .version(HttpVersion.from(parts[2]))
                .headers(parseHeaders(
                        Arrays.copyOfRange(raw, 1, raw.length)
                ))
                .body(body)
                .build();
    }

    private static Map<String, String> parseHeaders(String[] raw) {
        var parsed = new HashMap<String, String>();

        if (raw.length == 0) {
            throw HttpException.badRequest("Bad Headers");
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

    private static String[] parseRequestLine(String line) {
        if (line.length() > MAX_REQUEST_LINE_LEN) {
            throw HttpException.badRequest("Bad headers");
        }

        var parts = line.split(String.valueOf(SP), -1);

        if (parts.length != 3) {
            throw HttpException.badRequest("Bad headers");
        }

        for (String part : parts) {
            if (part.isEmpty()) {
                throw HttpException.badRequest("Bad headers");
            }
        }

        var method = parts[0];
        var version = parts[2];

        if (!VALID_METHODS.contains(method)) {
            throw HttpException.badRequest("Bad headers");
        }
        if (!VALID_VERSIONS.contains(version)) {
            throw HttpException.badRequest("Bad headers");
        }

        return parts;
    }
}
