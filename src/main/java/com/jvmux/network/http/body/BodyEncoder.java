package com.jvmux.network.http.body;

public interface BodyEncoder {

    byte[] encode(Object content);

    String contentType();
}
