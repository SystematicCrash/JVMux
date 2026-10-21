package com.jvmux.network.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static com.jvmux.network.tcp.Constants.CLIENT_IDLE_TIMEOUT;
import static com.jvmux.network.tcp.Constants.TCP_NO_DELAY;

public class TCPClient implements AutoCloseable {

    private final Socket socket;

    public TCPClient() {
        this.socket = new Socket();
    }

    public Socket connect(String host, int port) throws IOException {
        socket.setTcpNoDelay(TCP_NO_DELAY);
        socket.setSoTimeout(CLIENT_IDLE_TIMEOUT);
        socket.connect(new InetSocketAddress(host, port), CLIENT_IDLE_TIMEOUT);

        return this.socket;
    }

    public boolean isClosed() {
        return this.socket.isClosed();
    }

    @Override
    public void close() throws Exception {
        this.socket.close();
    }
}
