package com.jvmux.network.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static com.jvmux.network.tcp.Constants.IDLE_TIMEOUT;
import static com.jvmux.network.tcp.Constants.TCP_NO_DELAY;

public class TCPServer implements AutoCloseable {

    private final int port;
    private ServerEnvironment environment;
    private volatile boolean running = false;
    private final ConnectionHandler connHandler;

    public TCPServer(int port, ConnectionHandler processor) {
        this.port = port;
        this.connHandler = processor;
    }

    public void start() throws IOException {
        this.running = true;

        try (var env = ServerEnvironment.open(this.port)) {
            this.environment = env;

            while (running && !this.environment.serverSocket().isClosed()) {
                Socket clientSocket = this.environment.serverSocket().accept();
                this.environment.executor().submit(() -> this.handle(clientSocket));
            }
        }
    }

    private void handle(Socket socket) {
        try (socket) {
            socket.setTcpNoDelay(TCP_NO_DELAY);
            socket.setSoTimeout(IDLE_TIMEOUT);

            var in = socket.getInputStream();
            var out = socket.getOutputStream();

            while (!socket.isClosed() && this.running) {
                boolean keepAlive = this.connHandler.handle(in, out);
                if (!keepAlive) break;
            }
        } catch (SocketTimeoutException | EOFException ignored) {
        } catch (Exception e) {
            System.err.println("Client connection terminated: " + e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        this.running = false;
        if (this.environment != null) {
            this.environment.close();
        }
    }
}