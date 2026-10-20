package com.jvmux.network.inbound;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@AllArgsConstructor
public class InboundHandler {
    private final InputStream in;
    private final OutputStream out;

    public static InboundHandler from(Socket socket) throws IOException {
        return new InboundHandler(socket.getInputStream(), socket.getOutputStream());
    }
}
