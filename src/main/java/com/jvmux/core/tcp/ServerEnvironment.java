package com.jvmux.core.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public record ServerEnvironment(ServerSocket serverSocket, ExecutorService executor) implements AutoCloseable {

    public static ServerEnvironment open(int port) throws IOException {
        return new ServerEnvironment(
                new ServerSocket(port),
                Executors.newVirtualThreadPerTaskExecutor()
        );
    }

    @Override
    public void close() throws IOException {
        try {
            serverSocket.close();
        } finally {
            executor.close();
        }
    }
}