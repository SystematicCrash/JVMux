package com.jvmux.net.http.body;

public interface BodyEncoder {

    byte[] encode(Object content);

    String contentType();
}
