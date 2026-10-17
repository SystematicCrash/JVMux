package com.jvmux.core.http;

import com.jvmux.core.http.encoder.ResponseEncoder;
import com.jvmux.core.http.error.ErrorHandler;
import com.jvmux.core.http.error.HttpException;
import com.jvmux.core.http.model.HttpResponse;
import com.jvmux.core.http.parser.RequestParser;
import com.jvmux.core.routing.RouteTree;
import lombok.AllArgsConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.jvmux.core.http.constant.Constants.*;

@AllArgsConstructor
public class HttpServer {

    private final RouteTree routeTree;

    public boolean handle(InputStream in, OutputStream out) throws IOException {
        String[] raw;

        try {
            raw = this.readRequest(in);
            throw HttpException.badRequest("Fuck you");

        } catch (HttpException e) {
            this.writeResponse(out, ErrorHandler.toResponse(e));
            return false;
        }

//        if (raw == null) {
//            return false;
//        }
//
//        var response = this.dispatch(raw);
//
//        this.writeResponse(out, response);
//        return response.status() < 400;
    }

    private HttpResponse dispatch(String[] raw) {
        try {
            var request = RequestParser.parse(raw);
            var handler = this.routeTree.lookup(
                    request.startLine().method(), request.startLine().path()
            );

            return HttpResponse.builder()
                    .status(429)
                    .version(request.startLine().version())
                    .build();
        } catch (Exception error) {
            return ErrorHandler.toResponse(error);
        }
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

    private void writeResponse(OutputStream out, HttpResponse response) throws IOException {
        out.write(ResponseEncoder.encode(response));
    }
}
