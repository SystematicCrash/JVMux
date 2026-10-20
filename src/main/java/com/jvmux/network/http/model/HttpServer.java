package com.jvmux.network.http.model;

import com.jvmux.network.http.codec.RequestParser;
import com.jvmux.network.http.codec.ResponseEncoder;
import com.jvmux.network.http.exception.ErrorHandler;
import com.jvmux.network.http.exception.HttpException;
import com.jvmux.network.routing.RouteTree;
import lombok.AllArgsConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.jvmux.network.http.constant.Constants.*;

@AllArgsConstructor
public class HttpServer {

    private final RouteTree routeTree;

    public boolean handle(InputStream in, OutputStream out) {
        try {
            var request = this.readRequest(in);
            var response = this.dispatch(request);
            return writeResponse(response, out);
        } catch (HttpException | IOException e) {
            return this.writeResponse(ErrorHandler.toResponse(e), out);
        }
    }

    private HttpResponse dispatch(HttpRequest request) {
        try {
            var handler = this.routeTree.lookup(
                    request.method(), request.path()
            );

            return HttpResponse.text(400, "fuck you");
        } catch (Exception error) {
            return ErrorHandler.toResponse(error);
        }
    }

    private HttpRequest readRequest(InputStream in) throws IOException {
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

        var raw = buffer
                .toString(StandardCharsets.US_ASCII)
                .split(CRLF);

        return RequestParser.parse(raw, in);
    }

    private boolean writeResponse(HttpResponse response, OutputStream out) {
        try {
            out.write(ResponseEncoder.encode(response));
            response.getWriter().write(out);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
