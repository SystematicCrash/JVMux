package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WeightedRoundRobinBalancer implements LoadBalancer {

    private final ConcurrentHashMap<String, AtomicInteger> currentWeights = new ConcurrentHashMap<>();

    @Override
    public Server select(Cluster cluster) {
        List<Server> healthy = cluster.servers().stream()
                .filter(Server::isHealthy)
                .toList();

        if (healthy.isEmpty()) {
            throw new IllegalStateException("No healthy servers in cluster: " + cluster.name());
        }

        int totalWeight = 0;
        Server best = null;
        int maxWeight = Integer.MIN_VALUE;

        for (Server server : healthy) {
            totalWeight += server.getWeigth();

            String key = cluster.name() + ":" + server.getUrl();
            AtomicInteger cur = currentWeights
                    .computeIfAbsent(key, k -> new AtomicInteger(0));

            int updatedWeight = cur.addAndGet(server.getWeigth());

            if (updatedWeight > maxWeight) {
                maxWeight = updatedWeight;
                best = server;
            }
        }

        if (best != null) {
            String bestKey = cluster.name() + ":" + best.getUrl();
            currentWeights.get(bestKey).addAndGet(-totalWeight);
        }

        return best;
    }
}