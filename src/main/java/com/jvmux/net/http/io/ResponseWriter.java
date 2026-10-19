package com.jvmux.net.http.io;

import com.jvmux.net.http.constant.HttpHeader;
import com.jvmux.net.http.model.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.jvmux.net.http.constant.Constants.CRLF;
import static com.jvmux.net.http.constant.Constants.READ_BUFFER_SIZE;

public class ResponseWriter {

    public static void write(HttpResponse response, OutputStream out) throws IOException {
        out.write(response.encode());

        var transferEncoding = response.getHeader(HttpHeader.TRANSFER_ENCODING);
        if (Objects.equals(transferEncoding, "chunked")) {
            streamChunked(response.body(), out);
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

    private static void streamChunked(InputStream body, OutputStream out) throws IOException {
        var buffer = new byte[READ_BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = body.read(buffer)) != -1) {
            out.write(Integer.toHexString(bytesRead).getBytes(StandardCharsets.US_ASCII));
            out.write(CRLF.getBytes(StandardCharsets.US_ASCII));
            out.write(buffer, 0, bytesRead);
            out.write(CRLF.getBytes(StandardCharsets.US_ASCII));
        }

        out.write("0".getBytes(StandardCharsets.US_ASCII));
        out.write(CRLF.getBytes(StandardCharsets.US_ASCII));
        out.write(CRLF.getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }
}
