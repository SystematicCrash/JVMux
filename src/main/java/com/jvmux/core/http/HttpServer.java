package com.jvmux.core.http;

import com.jvmux.core.http.error.HttpException;
import com.jvmux.core.http.model.HttpRequest;
import com.jvmux.core.routing.RouteTree;
import lombok.AllArgsConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.jvmux.core.http.constant.Constants.*;
import static com.jvmux.core.http.parser.HeaderParser.parseHeaders;
import static com.jvmux.core.http.parser.RequestLineParser.parseRequestLine;

@AllArgsConstructor
public class HttpServer {

    private final RouteTree routeTree;

    public boolean handle(InputStream in, OutputStream out) throws IOException {
        var raw = this.readRequest(in);

        if (raw == null) {
            return false;
        }

        var request = this.parseRequest(raw);
        var handler = this.routeTree.lookup(request.method(), request.path());

        return true;
    }

    private HttpRequest parseRequest(String[] raw) {
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

    private String[] readRequest(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int state = 0;

        while (state < 4) {
            int b = in.read();

            if (b == -1) {
                if (buffer.size() == 0) {
                    return null;
                }
                throw new EOFException("Unexpected EOF while reading request");
            }

            buffer.write(b);

            if (buffer.size() > MAX_HEADER_SIZE) {
                throw HttpException.headersToLarge();
            }

            if ((state == 0 || state == 2) && b == CR) {
                state++;
            } else if ((state == 1 || state == 3) && b == LF) {
                state++;
            } else {
                state = (b == '\r') ? 1 : 0;
            }
        }

        return buffer
                .toString(StandardCharsets.US_ASCII)
                .split(CRLF);
    }
}
