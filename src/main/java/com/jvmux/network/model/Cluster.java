package com.jvmux.network.model;

import java.util.Set;

public record Cluster(Set<Server> servers) {
    public Server getRandomServer() {
        return new Server("localhost", 1234);
    }
}
