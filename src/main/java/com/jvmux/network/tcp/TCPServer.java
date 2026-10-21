package com.jvmux.network.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static com.jvmux.network.tcp.Constants.SERVER_IDLE_TIMEOUT;
import static com.jvmux.network.tcp.Constants.TCP_NO_DELAY;

public class TCPServer implements AutoCloseable {

    private final ServerSocket serverSocket;

    public TCPServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public Socket accept() throws IOException {
        var socket = this.serverSocket.accept();

        socket.setSoTimeout(SERVER_IDLE_TIMEOUT);
        socket.setTcpNoDelay(TCP_NO_DELAY);

        return socket;
    }

    public boolean isClosed() {
        return this.serverSocket.isClosed();
    }

    @Override
    public void close() throws IOException {
        this.serverSocket.close();
    }
}