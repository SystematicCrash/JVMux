package com.jvmux.core.http.encoder;

import com.jvmux.core.http.model.RequestLine;

import java.nio.charset.StandardCharsets;

import static com.jvmux.core.http.constant.Constants.SP;

public class RequestLineEncoder {

    public static byte[] encodeRequestLine(RequestLine requestLine) {
        var delim = String.valueOf(SP);
        var encoded =
                requestLine.method()
                + delim
                + requestLine.path()
                + delim
                + requestLine.version();

        return encoded.getBytes(StandardCharsets.US_ASCII);
    }
}
