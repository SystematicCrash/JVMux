package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.Comparator;
import java.util.List;

public class WeightedLeastConnectionBalancer implements LoadBalancer {

    @Override
    public Server select(Cluster cluster) {
        List<Server> healthy = cluster.servers().stream()
                .filter(Server::isHealthy)
                .toList();

        if (healthy.isEmpty()) {
            throw new IllegalStateException("No healthy servers in cluster: " + cluster.name());
        }

        if (healthy.size() == 1) {
            return healthy.getFirst();
        }

        return healthy.stream()
                .min(Comparator.comparingDouble(s ->
                        (double) s.getActiveConns().get() / Math.max(1, s.getWeight())
                ))
                .orElse(healthy.getFirst());
    }
}