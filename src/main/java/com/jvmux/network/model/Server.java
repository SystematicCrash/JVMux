package com.jvmux.network.model;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class Server {
    private static final double EWMA_ALPHA = 0.2;

    private final AtomicInteger activeConns = new AtomicInteger(0);
    private final AtomicLong responseTimeEwmaNanos = new AtomicLong(0);
    private volatile boolean healthy;
    private final byte weight;
    private final String host;
    private final String url;
    private final int port;

    public Server(String host, int port, byte weight) {
        if (weight < 1 || weight > 10) {
            throw new IllegalArgumentException("Server weight can only be between 1 and 10");
        }

        this.weight = weight;
        this.host = host;
        this.port = port;
        this.url = (port == 80 || port == 443)
                ? host
                : host + ":" + port;
    }

    public Server(String host, int port, byte weight, boolean healthy) {
        this(host, port, weight);
        this.healthy = healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public int getActiveConns() {
        return this.activeConns.get();
    }

    public void incConns() {
        this.activeConns.incrementAndGet();
    }

    public void decConns() {
        this.activeConns.decrementAndGet();
    }

    public void recordResponseTime(long responseTimeNanos) {
        this.responseTimeEwmaNanos.updateAndGet(prev ->
                prev == 0
                        ? responseTimeNanos
                        : (long) (EWMA_ALPHA * responseTimeNanos + (1 - EWMA_ALPHA) * prev));
    }
}
