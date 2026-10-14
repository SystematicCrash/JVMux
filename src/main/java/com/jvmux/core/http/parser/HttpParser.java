package com.jvmux.core.http.parser;

import com.jvmux.core.http.constant.HttpMethod;
import com.jvmux.core.http.constant.HttpVersion;
import com.jvmux.core.http.model.HttpRequest;
import com.jvmux.core.http.model.RequestLine;
import java.util.*;

import static com.jvmux.core.http.parser.HeaderParser.parseHeaders;
import static com.jvmux.core.http.parser.RequestLineParser.parseRequestLine;

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
}
