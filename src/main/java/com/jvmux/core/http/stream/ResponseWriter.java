package com.jvmux.core.http.stream;

import com.jvmux.core.http.constant.HttpHeader;
import com.jvmux.core.http.model.HttpResponse;

import java.io.*;
import java.util.Objects;

import static com.jvmux.core.http.constant.Constants.READ_BUFFER_SIZE;

public class ResponseWriter {

    public static void write(HttpResponse response, OutputStream out) throws IOException {
        out.write(response.encode());

        var transferEncoding = response.getHeader(HttpHeader.TRANSFER_ENCODING);
        if (Objects.equals(transferEncoding, "chunked")) {
            streamChunked(response.body(), out);
        } else {
            var contentLength = response.getHeader(HttpHeader.CONTENT_LENGTH);
            streamFixedLength(response.body(), out, Integer.parseInt(contentLength));
        }
    }

    public static void streamFixedLength(InputStream in, OutputStream out, long contentLength) throws IOException {
        var remaining = contentLength;
        var buffer = new byte[READ_BUFFER_SIZE];

        while (remaining > 0) {
            var toRead = (int) Math.min(remaining, buffer.length);
            var bytes = in.read(buffer, 0, toRead);

            if (bytes == -1) {
                throw new EOFException("Unexpected EOF while reading");
            }
            out.write(buffer, 0, bytes);
            remaining -= bytes;
        }
        out.flush();
    }

    public static void streamChunked(InputStream in, OutputStream outputStream) {
        // TODO
    }
}
