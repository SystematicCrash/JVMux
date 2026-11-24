package com.jvmux.network.model;

import com.jvmux.network.balancer.BalancingStrategy;

import java.util.Objects;
import java.util.Set;

public record Cluster(
        String name,
        Set<Server> servers,
        BalancingStrategy strategy
) {
    public Cluster(String name, Set<Server> servers) {
        this(name, servers, BalancingStrategy.ROUND_ROBIN);
    }

    public Cluster {
        Objects.requireNonNull(name, "Cluster name cannot be null");
        servers = servers != null ? Set.copyOf(servers) : Set.of();
        if (strategy == null) {
            strategy = BalancingStrategy.ROUND_ROBIN;
        }
    }

    public Server getRandomServer() {
        return new Server("localhost", 1234, (byte) 5);
    }
}
