package com.jvmux.core.tcp;

import lombok.AllArgsConstructor;
import java.io.IOException;

@AllArgsConstructor
public class Server {
    private final int port;
    private final ConnectionHandler handler;

    public void start() throws IOException {
        try (var env = ServerEnvironment.open(this.port)) {
            while (!env.serverSocket().isClosed()) {
                var clientSocket = env.serverSocket().accept();
                env.executor().submit(() -> this.handler.handle(clientSocket));
            }
        }
    }

}
