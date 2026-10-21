package com.jvmux.network.http.codec.payload;

public interface PayloadEncoder {

    byte[] encode(Object content);

    String contentType();
}
