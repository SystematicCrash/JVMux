package com.jvmux.network.http.codec.encode;

import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.model.HttpRequest;

import java.nio.charset.StandardCharsets;

import static com.jvmux.network.http.constant.Constants.*;

public class RequestEncoder {

    public static byte[] encode(HttpRequest request) {
        var sb = new StringBuilder();

        sb.append(encodeRequestLine(request.version(), request.path(), request.method()));

        for (var header : request.headers().entrySet()) {
            sb.append(header.getKey())
                    .append(COLON)
                    .append(SP)
                    .append(header.getValue())
                    .append(CRLF);
        }
        sb.append(CRLF);

        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private static String encodeRequestLine(HttpVersion version, String target, HttpMethod method) {
        var delim = String.valueOf(SP);
        return method.value()
                + delim
                + target
                + delim
                + version.value()
                + CRLF;
    }
}
