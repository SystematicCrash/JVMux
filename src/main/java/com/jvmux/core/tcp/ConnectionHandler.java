package com.jvmux.core.tcp;

import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler {

    public void handle(Socket socket) {
        try (socket) {
            socket.setSoTimeout(15000);
            socket.setTcpNoDelay(true);

            var in = socket.getInputStream();
            var out = socket.getOutputStream();

            while (!socket.isClosed()) {
                // TODO: call processor
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
