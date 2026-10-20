package com.jvmux.network.outbound;

import lombok.AllArgsConstructor;

import java.io.InputStream;
import java.io.OutputStream;

@AllArgsConstructor
public class OutboundHandler {
    private final InputStream in;
    private final OutputStream out;
}
