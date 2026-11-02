package com.jvmux.network.http.codec.decode;

import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.model.HttpRequest;

import java.io.InputStream;
import java.util.Arrays;

import static com.jvmux.network.http.constant.Constants.*;

public class RequestParser {

    public static HttpRequest parse(String[] raw, InputStream body) {
        if (raw == null || raw.length == 0) {
            throw new IllegalArgumentException("Empty HTTP request: no request-line or headers received");
        }

        var parts = parseRequestLine(raw[0]);

        return HttpRequest.builder()
                .method(HttpMethod.from(parts[0]))
                .path(parts[1])
                .version(HttpVersion.from(parts[2]))
                .headers(HeaderParser.parseRequestHeaders(
                        Arrays.copyOfRange(raw, 1, raw.length)
                ))
                .body(body)
                .build();
    }

    private static String[] parseRequestLine(String line) {
        if (line == null || line.isEmpty()) {
            throw new IllegalArgumentException("Malformed request-line: line is empty");
        }

        if (line.length() > MAX_REQUEST_LINE_LEN) {
            throw new IllegalArgumentException(String.format(
                    "Request-line length (%d bytes) exceeds maximum allowed limit (%d bytes)",
                    line.length(), MAX_REQUEST_LINE_LEN
            ));
        }

        var parts = extractRequestLineParts(line);

        var method = parts[0];
        var version = parts[2];

        if (!VALID_METHODS.contains(method)) {
            throw new IllegalArgumentException(String.format(
                    "Unsupported or invalid HTTP method '%s'. Allowed methods: %s",
                    method, VALID_METHODS
            ));
        }

        if (!SUPPORTED_VERSIONS.contains(version)) {
            throw new IllegalArgumentException(String.format(
                    "Unsupported HTTP version '%s'. Supported versions: %s",
                    version, SUPPORTED_VERSIONS
            ));
        }

        return parts;
    }

    private static String[] extractRequestLineParts(String line) {
        var parts = line.split(String.valueOf(SP), -1);

        if (parts.length != 3) {
            throw new IllegalArgumentException(String.format(
                    "Malformed request-line: expected exactly 3 space-separated tokens [METHOD, TARGET, VERSION] but received %d in '%s'",
                    parts.length, line
            ));
        }

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                throw new IllegalArgumentException(String.format(
                        "Malformed request-line: empty token at position %d (possible leading, trailing, or consecutive spaces) in '%s'",
                        i, line
                ));
            }
        }
        return parts;
    }
}