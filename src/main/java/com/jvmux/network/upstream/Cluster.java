package com.jvmux.network.upstream;

import java.util.Set;

public record Cluster(String path, Set<Server> servers) {
    public Server getRandomServer() {
        return new Server("localhost", 1234);
    }
}
