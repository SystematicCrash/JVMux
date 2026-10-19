package com.jvmux.network.tcp;

import lombok.AllArgsConstructor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.function.BiFunction;

@AllArgsConstructor
public class TCPServer {
    private final int port;
    private final BiFunction<InputStream, OutputStream, Boolean> handler;

    public void start() throws IOException {
        try (var env = ServerEnvironment.open(this.port)) {
            while (!env.serverSocket().isClosed()) {
                var clientSocket = env.serverSocket().accept();
                env.executor().submit(() -> this.handle(clientSocket));
            }
        }
    }

    public void handle(Socket socket) {
        try (socket) {
            socket.setSoTimeout(3000);
            socket.setTcpNoDelay(true);

            var keepAlive = true;

            while (!socket.isClosed() && keepAlive) {
                keepAlive = this.handler.apply(
                        socket.getInputStream(),
                        socket.getOutputStream()
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
