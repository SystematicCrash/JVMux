package com.jvmux.network.http.codec.decode;

import com.jvmux.network.http.constant.HttpStatus;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.model.HttpResponse;

import java.io.InputStream;
import java.util.Arrays;

import static com.jvmux.network.http.constant.Constants.*;

public class ResponseParser {

    public static HttpResponse parse(String[] raw, InputStream body) {
        if (raw == null || raw.length == 0) {
            throw new IllegalArgumentException("Empty HTTP response: no status-line or headers received");
        }

        var parts = parseResponseLine(raw[0]);

        int statusCode;
        try {
            statusCode = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Malformed status code: '%s' is not an integer", parts[1]), e
            );
        }

        var headers = HeaderParser.parseResponseHeaders(
                Arrays.copyOfRange(raw, 1, raw.length)
        );

        return HttpResponse.builder()
                .version(HttpVersion.from(parts[0]))
                .status(statusCode)
                .headers(headers)
                .body(body)
                .build();
    }

    private static String[] parseResponseLine(String line) {
        if (line == null || line.isEmpty()) {
            throw new IllegalArgumentException("Malformed status-line: line is empty");
        }

        var parts = line.split(String.valueOf(SP), 3);

        if (parts.length < 2) {
            throw new IllegalArgumentException(String.format(
                    "Malformed status-line: expected at least 2 tokens [VERSION, STATUS_CODE] but received %d in '%s'",
                    parts.length, line
            ));
        }

        var version = parts[0];
        if (!VALID_VERSIONS.contains(version)) {
            throw new IllegalArgumentException(String.format(
                    "Unsupported or invalid HTTP version '%s'. Allowed: %s",
                    version, VALID_VERSIONS
            ));
        }

        try {
            int statusCode = Integer.parseInt(parts[1]);
            if (!HttpStatus.isValid(statusCode)) {
                throw new IllegalArgumentException(String.format(
                        "Invalid HTTP status code %d: must be between 100 and 599",
                        statusCode
                ));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "Malformed status code: '%s' is not an integer",
                    parts[1]
            ), e);
        }

        return parts;
    }
}