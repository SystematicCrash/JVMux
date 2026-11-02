package com.jvmux.network.http.io;

import com.jvmux.network.http.exception.HttpException;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.jvmux.network.http.constant.Constants.*;

public final class HeaderReader {

    private HeaderReader() {}

    public static String[] readHeaders(InputStream in, boolean isUpstream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int state = 0;

        while (state < 4) {
            int b = in.read();

            if (b == -1) {
                if (buffer.size() == 0) {
                    return null;
                }
                if (isUpstream) {
                    throw HttpException.badGateway();
                }
                throw new EOFException("Unexpected EOF while reading request headers");
            }

            buffer.write(b);

            if (buffer.size() > MAX_HEADER_SIZE) {
                if (isUpstream) {
                    throw HttpException.badGateway();
                }
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

        return buffer.toString(StandardCharsets.US_ASCII).split(CRLF);
    }
}