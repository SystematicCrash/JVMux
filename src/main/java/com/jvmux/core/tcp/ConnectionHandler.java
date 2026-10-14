package com.jvmux.core.tcp;

import com.jvmux.core.http.HttpServer;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.Socket;

@AllArgsConstructor
public class ConnectionHandler {

    private final HttpServer http;

    public void handle(Socket socket) {
        try (socket) {
            socket.setSoTimeout(15000);
            socket.setTcpNoDelay(true);

            var in = socket.getInputStream();
            var out = socket.getOutputStream();

            while (!socket.isClosed()) {
                http.handle(in, out);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
