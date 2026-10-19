package com.jvmux.network.http.codec;

import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.model.HttpResponse;
import com.jvmux.network.http.constant.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.jvmux.network.http.constant.Constants.COLON;
import static com.jvmux.network.http.constant.Constants.CRLF;
import static com.jvmux.network.http.constant.Constants.READ_BUFFER_SIZE;
import static com.jvmux.network.http.constant.Constants.SP;

public class ResponseEncoder {

    private static final Charset ASCII = StandardCharsets.US_ASCII;

    public static byte[] encode(HttpResponse response) {
        var str = new StringBuilder();

        str.append(encodeResponseLine(response.version(), response.status()));

        if (response.headers() != null) {
            for (var header : response.headers().entrySet()) {
                str.append(header.getKey())
                        .append(COLON)
                        .append(SP)
                        .append(header.getValue())
                        .append(CRLF);
            }
            str.append(CRLF);
        }

        return str.toString().getBytes(ASCII);
    }

    public static void encodeChunked(InputStream body, OutputStream out) throws IOException {
        var buffer = new byte[READ_BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = body.read(buffer)) != -1) {
            writeString(out, Integer.toHexString(bytesRead));
            writeString(out, CRLF);
            out.write(buffer, 0, bytesRead);
            writeString(out, CRLF);
        }

        writeString(out, "0");
        writeString(out, CRLF);
        writeString(out, CRLF);
        out.flush();
    }

    private static void writeString(OutputStream out, String s) throws IOException {
        out.write(s.getBytes(ASCII));
    }

    private static String encodeResponseLine(HttpVersion version, int status) {
        var delim = String.valueOf(SP);
        return version.toString()
                + delim
                + status
                + delim
                + HttpStatus.reasonPhrase(status)
                + CRLF;
    }
}
