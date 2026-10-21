package com.jvmux.network.http.io;

import com.jvmux.network.http.codec.RequestParser;
import com.jvmux.network.http.codec.ResponseEncoder;
import com.jvmux.network.http.exception.HttpException;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.jvmux.network.http.constant.Constants.*;

public class HttpServer {

    public static HttpRequest readRequest(InputStream in) throws IOException {
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

    public static void writeResponse(HttpResponse response, OutputStream out) throws IOException {
        out.write(ResponseEncoder.encode(response));
        response.getWriter().write(out);
    }
}
