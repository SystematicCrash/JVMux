package com.jvmux.network.tcp;

import com.jvmux.network.model.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPProber {

    public static long measureRTT(Server server, int timeoutMs) {
        long start = System.nanoTime();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(
                    server.getHost(),
                    server.getPort()
            ), timeoutMs);
            return (System.nanoTime() - start) / 1_000_000;
        } catch (IOException e) {
            return -1;
        }
    }
}
