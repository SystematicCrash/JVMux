package com.jvmux.core.http.encoder;

import com.jvmux.core.http.constant.HttpVersion;
import com.jvmux.core.http.model.HttpStatus;

import java.nio.charset.StandardCharsets;

import static com.jvmux.core.http.constant.Constants.CRLF;
import static com.jvmux.core.http.constant.Constants.SP;

public class ResponseLineEncoder {

    public static byte[] encode(HttpVersion version, int status) {
        var delim = String.valueOf(SP);
        var encoded =
                version.toString()
                + delim
                + status
                + delim
                + HttpStatus.reasonPhrase(status)
                + CRLF;

        return encoded.getBytes(StandardCharsets.US_ASCII);
    }
}
