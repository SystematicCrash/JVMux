package com.jvmux.network.http.io;

import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.model.HttpResponse;
import com.jvmux.network.http.constant.HttpStatus;

import java.nio.charset.StandardCharsets;

import static com.jvmux.network.http.constant.Constants.*;

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
