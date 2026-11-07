package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomBalancer implements LoadBalancer {

    @Override
    public Server select(Cluster cluster) {
        List<Server> healthy = cluster.servers().stream()
                .filter(Server::isHealthy)
                .toList();

        if (healthy.isEmpty()) {
            throw new IllegalStateException("No healthy servers available in cluster: " + cluster.name());
        }

        if (healthy.size() == 1) {
            return healthy.getFirst();
        }

        int index = ThreadLocalRandom.current().nextInt(healthy.size());
        return healthy.get(index);
    }
}
