package com.jvmux.network.model;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class Server {
    private AtomicInteger activeConns;
    private volatile boolean healthy;
    private final String host;
    private final int port;

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String url() {
        return (port == 80 || port == 443)
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
