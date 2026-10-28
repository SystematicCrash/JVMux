package com.jvmux.network.http.codec.encode;

import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.model.HttpResponse;
import com.jvmux.network.http.constant.HttpStatus;

import java.nio.charset.StandardCharsets;

import static com.jvmux.network.http.constant.Constants.COLON;
import static com.jvmux.network.http.constant.Constants.CRLF;
import static com.jvmux.network.http.constant.Constants.SP;

public class ResponseEncoder {

    public static byte[] encode(HttpResponse response) {
        var sb = new StringBuilder();

        sb.append(encodeResponseLine(response.getVersion(), response.getStatus()));

        for (var header : response.getHeaders().entrySet()) {
            sb.append(header.getKey())
                    .append(COLON)
                    .append(SP)
                    .append(header.getValue())
                    .append(CRLF);
        }
        sb.append(CRLF);

        return sb.toString().getBytes(StandardCharsets.US_ASCII);
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
