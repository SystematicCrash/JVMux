package com.jvmux.net.http.body;

import java.nio.charset.StandardCharsets;

public class PlainTextEncoder implements BodyEncoder {

    @Override
    public byte[] encode(Object content) {
        return String.valueOf(content).getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public String contentType() {
        return "text/plain";
    }
}
