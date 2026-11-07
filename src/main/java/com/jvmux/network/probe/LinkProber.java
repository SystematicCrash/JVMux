package com.jvmux.network.probe;

import com.jvmux.network.model.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class LinkProber {

    public static long tcpRTT(Server server, int timeoutMs) {
        long start = System.nanoTime();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(server.host(), server.port()), timeoutMs);
            return (System.nanoTime() - start) / 1_000_000;
        } catch (IOException e) {
            return -1;
        }
    }
}
