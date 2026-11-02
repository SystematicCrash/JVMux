package com.jvmux.network.http.codec.payload;

import java.nio.charset.StandardCharsets;

public class PlainTextEncoder implements PayloadEncoder {

    @Override
    public byte[] encode(Object content) {
        return String.valueOf(content).getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public String contentType() {
        return "text/plain";
    }
}
