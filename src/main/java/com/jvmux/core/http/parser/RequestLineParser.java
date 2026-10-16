package com.jvmux.core.http.parser;

import com.jvmux.core.http.constant.HttpMethod;
import com.jvmux.core.http.constant.HttpVersion;
import com.jvmux.core.http.error.HttpException;
import com.jvmux.core.http.model.RequestLine;

import static com.jvmux.core.http.constant.Constants.*;
import static com.jvmux.core.http.constant.Constants.VALID_VERSIONS;

public class RequestLineParser {
    public static RequestLine parseRequestLine(String line) {
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
        var target = parts[1];
        var version = parts[2];

        if (!VALID_METHODS.contains(method)) {
            throw HttpException.badRequest("Bad headers");
        }
        if (!VALID_VERSIONS.contains(version)) {
            throw HttpException.badRequest("Bad headers");
        }

        return RequestLine.builder()
                .path(target)
                .method(HttpMethod.from(method))
                .version(HttpVersion.from(version))
                .build();
    }
}
