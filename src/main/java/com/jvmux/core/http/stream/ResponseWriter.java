package com.jvmux.core.http.stream;

import java.io.*;

import static com.jvmux.core.http.constant.Constants.READ_BUFFER_SIZE;

public class ResponseWriter {

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
}
