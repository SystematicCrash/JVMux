package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.List;

public class LeastConnectionBalancer implements LoadBalancer {

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

        Server best = null;
        int leastConnections = 0;
        for (Server server : healthy) {
            if (server.getActiveConns().get() < leastConnections)
                best = server;
        }

        return best;
    }
}
