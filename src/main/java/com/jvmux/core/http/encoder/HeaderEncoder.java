package com.jvmux.core.http.encoder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.jvmux.core.http.constant.Constants.CRLF;

public class HeaderEncoder {

    public static byte[] encoderHeaders(Map<String, String> headers) {
        var str = new StringBuilder();

        for (var header : headers.entrySet()) {
            str.append(header.getKey())
                    .append(":")
                    .append(header.getValue())
                    .append(CRLF);
        }

        return str.toString().getBytes(StandardCharsets.US_ASCII);
    }
}
