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
            socket.setSoTimeout(3000);
            socket.setTcpNoDelay(true);

            while (!socket.isClosed()) {
                http.handle(
                        socket.getInputStream(),
                        socket.getOutputStream()
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
