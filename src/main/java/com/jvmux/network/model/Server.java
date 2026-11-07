package com.jvmux.network.model;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class Server {
    private AtomicInteger activeConns;
    private volatile boolean healthy;
    private final byte weigth;
    private final String host;
    private final String url;
    private final int port;

    public Server(String host, int port, byte weight) {
        if (weight < 1 || weight > 10) {
            throw new IllegalArgumentException("Server weight can only be between 1 and 10");
        }

        this.weigth = weight;
        this.host = host;
        this.port = port;
        this.url = (port == 80 || port == 443)
                ? host
                : host + ":" + port;
    }

    public void incConns() {
        this.activeConns.incrementAndGet();
    }

    public void decConns() {
        this.activeConns.decrementAndGet();
    }
}
