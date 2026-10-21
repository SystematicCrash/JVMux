package com.jvmux.network.http.codec;

import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.exception.HttpException;
import com.jvmux.network.http.model.HttpRequest;

import java.io.InputStream;
import java.util.Arrays;
import static com.jvmux.network.http.constant.Constants.*;

public class RequestParser {

    public static HttpRequest parse(String[] raw) {
        return parse(raw, null);
    }

    public static HttpRequest parse(String[] raw, InputStream body) {
        if (raw.length == 0) {
            throw HttpException.badRequest("Bad Request");
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
        if (line.length() > MAX_REQUEST_LINE_LEN) {
            throw HttpException.badRequest("Invalid Request Line");
        }

        var parts = line.split(String.valueOf(SP), -1);

        if (parts.length != 3) {
            throw HttpException.badRequest("Invalid Request Line");
        }

        for (String part : parts) {
            if (part.isEmpty()) {
                throw HttpException.badRequest("Invalid Request Line");
            }
        }

        var method = parts[0];
        var version = parts[2];

        if (!VALID_METHODS.contains(method)) {
            throw HttpException.badRequest("Bad headers");
        }
        if (!SUPPORTED_VERSIONS.contains(version)) {
            throw HttpException.badRequest("Bad headers");
        }

        return parts;
    }
}
