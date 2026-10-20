package com.jvmux.network.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static com.jvmux.network.tcp.Constants.CLIENT_IDLE_TIMEOUT;
import static com.jvmux.network.tcp.Constants.TCP_NO_DELAY;

public class TCPClient {

    public <T> T execute(String host, int port, ConnectionProcessor<T> connHandler) {
        try(var socket = new Socket()) {
            socket.setTcpNoDelay(TCP_NO_DELAY);
            socket.setSoTimeout(CLIENT_IDLE_TIMEOUT);
            socket.connect(new InetSocketAddress(host, port), CLIENT_IDLE_TIMEOUT);

            return connHandler.process(socket.getInputStream(), socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
