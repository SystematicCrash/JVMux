package com.jvmux.net.http.io;

import com.jvmux.net.http.constant.HttpHeader;
import com.jvmux.net.http.model.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import static com.jvmux.net.http.constant.Constants.READ_BUFFER_SIZE;

public class ResponseWriter {

    public static void write(HttpResponse response, OutputStream out) throws IOException {
        out.write(ResponseEncoder.encode(response));

        var transferEncoding = response.getHeader(HttpHeader.TRANSFER_ENCODING);
        if (Objects.equals(transferEncoding, "chunked")) {
            ResponseEncoder.encodeChunked(response.body(), out);
        } else {
            var contentLength = response.getHeader(HttpHeader.CONTENT_LENGTH);
            streamFixedLength(response.body(), out, Long.parseLong(contentLength));
        }
    }

    private static void streamFixedLength(InputStream in, OutputStream out, long contentLength) throws IOException {
        var remaining = contentLength;
        var buffer = new byte[READ_BUFFER_SIZE];

        while (remaining > 0) {
            var toRead = (int) Math.min(remaining, buffer.length);
            var bytes = in.read(buffer, 0, toRead);

            if (bytes == -1) {
                throw new IOException("Unexpected EOF while reading");
            }
            out.write(buffer, 0, bytes);
            remaining -= bytes;
        }
        out.flush();
    }
}
