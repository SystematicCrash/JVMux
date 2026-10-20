package com.jvmux.network.inbound;

import lombok.AllArgsConstructor;

import java.io.InputStream;
import java.io.OutputStream;

@AllArgsConstructor
public class InboundHandler {
    private final InputStream in;
    private final OutputStream out;
}
