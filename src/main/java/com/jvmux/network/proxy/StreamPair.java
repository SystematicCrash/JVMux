package com.jvmux.network.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public record StreamPair(InputStream in, OutputStream out) {

    public static StreamPair from(Socket socket) throws IOException {
        return new StreamPair(socket.getInputStream(), socket.getOutputStream());
    }
}
