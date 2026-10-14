package com.jvmux.core.http;

import com.jvmux.core.http.parser.HttpParser;
import com.jvmux.core.routing.RouteTree;
import lombok.AllArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static com.jvmux.core.http.constant.Constants.*;

@AllArgsConstructor
public class HttpServer {

    private final RouteTree routeTree;

    public void handle(InputStream in, OutputStream out) throws IOException {
        var raw = this.readRequest(in);

        if (raw == null) {
            return;
        }

        var request = HttpParser.parseRequest(raw);
        var handler = this.routeTree.lookup(request.method(), request.path());
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
                throw new IOException("Client closed connection prematurely before completing HTTP header");
            }

            buffer.write(b);

            if (buffer.size() > MAX_HEADER_SIZE) {
                throw new IOException("HTTP header section exceeds maximum allowed size (" + MAX_HEADER_SIZE + " bytes)");
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
