package com.jvmux.network.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static com.jvmux.network.tcp.Constants.CLIENT_IDLE_TIMEOUT;
import static com.jvmux.network.tcp.Constants.TCP_NO_DELAY;

public class TCPClient {

    public static Socket connect(String host, int port) throws IOException {
        var socket = new Socket();
        socket.setTcpNoDelay(TCP_NO_DELAY);
        socket.setSoTimeout(CLIENT_IDLE_TIMEOUT);
        socket.connect(new InetSocketAddress(host, port), CLIENT_IDLE_TIMEOUT);

        return socket;
    }
}
