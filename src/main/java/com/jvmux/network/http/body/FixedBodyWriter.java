package com.jvmux.network.http.body;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.jvmux.network.http.constant.Constants.READ_BUFFER_SIZE;

public class FixedBodyWriter extends BodyWriter {
    private final long length;

    public FixedBodyWriter(InputStream inputStream, long length) {
        super(inputStream);
        this.length = length;
    }

    @Override
    public void write(OutputStream clientOut) throws IOException {
        var remaining = this.length;
        var buffer = new byte[READ_BUFFER_SIZE];

        while (remaining > 0) {
            var toRead = (int) Math.min(remaining, buffer.length);
            var bytes = inputStream.read(buffer, 0, toRead);

            if (bytes == -1) {
                throw new IOException("Unexpected EOF while reading");
            }
            clientOut.write(buffer, 0, bytes);
            remaining -= bytes;
        }
        clientOut.flush();
    }
}
