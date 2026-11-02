package com.jvmux.network.model;

import java.net.InetSocketAddress;
import java.net.Socket;

public record Client(
        String clientIp,
        int clientPort,
        int localPort
) {
    public static Client from(Socket socket) {
        var remote = (InetSocketAddress) socket.getRemoteSocketAddress();
        var local = (InetSocketAddress) socket.getLocalSocketAddress();
        return new Client(
                remote.getAddress().getHostAddress(),
                remote.getPort(),
                local.getPort()
        );
    }
}
