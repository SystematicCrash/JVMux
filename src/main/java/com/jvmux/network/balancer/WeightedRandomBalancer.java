package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedRandomBalancer implements LoadBalancer {

    @Override
    public Server select(Cluster cluster) {
        List<Server> healthy = cluster.servers().stream()
                .filter(Server::isHealthy)
                .toList();

        if (healthy.isEmpty()) {
            throw new IllegalStateException("No healthy servers in cluster: " + cluster.name());
        }

        var size = healthy.size();
        if (size == 1) {
            return healthy.getFirst();
        }

        int totalWeight = healthy.stream().mapToInt(Server::getWeight).sum();
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int cursor = 0;

        for (Server server : healthy) {
            cursor += server.getWeight();
            if (cursor >= random) {
                return server;
            }
        }

        return healthy.get(ThreadLocalRandom.current().nextInt(size));
    }
}
