package com.jvmux.network.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static com.jvmux.network.tcp.Constants.SERVER_IDLE_TIMEOUT;
import static com.jvmux.network.tcp.Constants.TCP_NO_DELAY;

public class TCPServer implements AutoCloseable {

    private final ServerSocket serverSocket;

    public TCPServer(String host, int port, int backlog) throws IOException {
        this.serverSocket = new ServerSocket(port, backlog, InetAddress.getByName(host));
    }

    public TCPServer(String host, int port) throws IOException {
        this(host, port, 1024);
    }

    public TCPServer(int port) throws IOException {
        this("0.0.0.0", port, 1024);
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

    public int getPort() {
        return this.serverSocket.getLocalPort();
    }

    @Override
    public void close() throws IOException {
        this.serverSocket.close();
    }
}