package com.jvmux.network.http.body;

import com.jvmux.network.http.constant.Constants;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static com.jvmux.network.http.constant.Constants.*;

public class ChunkedBodyWriter extends BodyWriter {

    private final byte[] buffer = new byte[Constants.READ_BUFFER_SIZE];

    public ChunkedBodyWriter(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public void write(OutputStream clientOut) throws IOException {
        while (true) {
            String line = readLine(inputStream);

            clientOut.write(line.getBytes(StandardCharsets.US_ASCII));
            clientOut.write(CRLF.getBytes(StandardCharsets.US_ASCII));

            var chunkSize = parseChunkSize(line);

            if (chunkSize == 0) {
                pipeTrailers(inputStream, clientOut);
                clientOut.flush();
                break;
            }

            writeChunk(chunkSize, clientOut);
        }
    }

    private int parseChunkSize(String line) {
        int semicolonIdx = line.indexOf(SEMI_COLON);
        String hexSize = (semicolonIdx != -1) ? line.substring(0, semicolonIdx).trim() : line.trim();
        return Integer.parseInt(hexSize, 16);
    }

    private void writeChunk(int chunkSize, OutputStream clientOut) throws IOException {
        int remaining = chunkSize;

        while (remaining > 0) {
            int toRead = Math.min(remaining, buffer.length);
            int read = inputStream.read(buffer, 0, toRead);
            if (read == -1) {
                throw new EOFException("Premature EOF while reading upstream chunk data");
            }
            clientOut.write(buffer, 0, read);
            remaining -= read;
        }

        byte[] crlf = inputStream.readNBytes(2);
        clientOut.write(crlf);
        clientOut.flush();
    }

    private static String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == CR) {
                int next = in.read();
                if (next == LF) break;
                sb.append((char) b).append((char) next);
            } else {
                sb.append((char) b);
            }
        }
        return sb.toString();
    }

    private static void pipeTrailers(InputStream in, OutputStream out) throws IOException {
        while (true) {
            String line = readLine(in);
            out.write(line.getBytes(StandardCharsets.US_ASCII));
            out.write(Constants.CRLF.getBytes(StandardCharsets.US_ASCII));
            if (line.isEmpty()) {
                break;
            }
        }
    }
}
