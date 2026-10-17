package com.jvmux.core.http.encoder;

import com.jvmux.core.http.constant.HttpVersion;
import com.jvmux.core.http.model.HttpResponse;
import com.jvmux.core.http.model.HttpStatus;

import java.nio.charset.StandardCharsets;

import static com.jvmux.core.http.constant.Constants.CRLF;
import static com.jvmux.core.http.constant.Constants.SP;

public class ResponseEncoder {

    public static byte[] encode(HttpResponse response) {
        var str = new StringBuilder();

        str.append(encodeResponseLine(response.version(), response.status()))
                .append(CRLF);

        for (var header : response.headers().entrySet()) {
            str.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append(CRLF);
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
