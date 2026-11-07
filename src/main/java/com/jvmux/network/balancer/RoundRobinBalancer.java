package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinBalancer implements LoadBalancer {

    private final ConcurrentHashMap<String, AtomicInteger> pointers = new ConcurrentHashMap<>();

    @Override
    public Server select(Cluster cluster) {
        var servers = cluster.servers()
                .stream()
                .filter(Server::isHealthy)
                .toList();

        if (servers.isEmpty()) {
            throw new IllegalStateException("No healthy servers: " + cluster.name());
        }

        int size = servers.size();
        if (size == 1) {
            return servers.getFirst();
        }

        AtomicInteger counter = pointers.computeIfAbsent(cluster.name(), k -> new AtomicInteger(0));

        int index = counter.getAndUpdate(cur -> (cur + 1) % size);
        return servers.get(index);
    }
}