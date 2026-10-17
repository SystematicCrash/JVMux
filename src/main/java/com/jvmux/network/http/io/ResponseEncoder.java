package com.jvmux.network.http.io;

import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.model.HttpResponse;
import com.jvmux.network.http.constant.HttpStatus;

import java.nio.charset.StandardCharsets;

import static com.jvmux.network.http.constant.Constants.CRLF;
import static com.jvmux.network.http.constant.Constants.SP;

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

        return str.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private static String encodeHeader(String key, String value) {
        return key + ": " + value + CRLF;
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
