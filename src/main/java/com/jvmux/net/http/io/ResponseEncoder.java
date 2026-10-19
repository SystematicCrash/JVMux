package com.jvmux.net.http.io;

import com.jvmux.net.http.constant.HttpVersion;
import com.jvmux.net.http.model.HttpResponse;
import com.jvmux.net.http.constant.HttpStatus;

import java.nio.charset.StandardCharsets;

import static com.jvmux.net.http.constant.Constants.COLON;
import static com.jvmux.net.http.constant.Constants.CRLF;
import static com.jvmux.net.http.constant.Constants.SP;

public class ResponseEncoder {

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

        return str.toString().getBytes(StandardCharsets.US_ASCII);
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
