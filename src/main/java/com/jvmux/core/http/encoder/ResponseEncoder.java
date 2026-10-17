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

        str.append(encodeResponseLine(response.version(), response.status()));

        if (response.headers() != null) {
            for (var header : response.headers().entrySet()) {
                str.append(encodeHeader(
                        header.getKey(),
                        header.getValue()
                ));
            }
            str.append(CRLF);
        }

        str.append(encodeBody(response.body()));
        return str.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private static String encodeHeader(String key, String value) {
        return key + ": " + value + CRLF;
    }

    private static String encodeBody(String body) {
        if (body == null || body.isEmpty()) {
            return "";
        }

        return body + CRLF;
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
