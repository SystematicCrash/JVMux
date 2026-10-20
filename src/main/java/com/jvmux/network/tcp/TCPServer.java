package com.jvmux.network.tcp;

import com.jvmux.network.session.ConnectionSession;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements AutoCloseable {

    private final int port;
    private volatile ExecutorService executor;
    private volatile ServerSocket serverSocket;
    private volatile boolean running = false;

    public TCPServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        this.running = true;
        this.serverSocket = new ServerSocket(this.port);
        this.executor = Executors.newVirtualThreadPerTaskExecutor();

        try (this) {
            while (running && !serverSocket.isClosed()) {
                var clientSocket = serverSocket.accept();
                this.executor.submit(() -> ConnectionSession.from(clientSocket));
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.running = false;

        if (this.serverSocket != null)
            this.serverSocket.close();

        if (this.executor != null)
            this.executor.close();
    }
}