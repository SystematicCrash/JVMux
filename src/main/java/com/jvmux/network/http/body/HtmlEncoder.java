package com.jvmux.network.http.body;

import java.nio.charset.StandardCharsets;

public class HtmlEncoder implements BodyEncoder {

    @Override
    public byte[] encode(Object content) {
        return String.valueOf(content).getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public String contentType() {
        return "text/html";
    }
}
