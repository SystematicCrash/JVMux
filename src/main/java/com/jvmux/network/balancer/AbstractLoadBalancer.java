package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.List;

public abstract class AbstractLoadBalancer implements LoadBalancer {

    @Override
    public final Server select(Cluster cluster) {
        List<Server> healthy = healthyServers(cluster);

        if (healthy.size() == 1) {
            return healthy.getFirst();
        }

        return selectFrom(cluster, healthy);
    }

    protected abstract Server selectFrom(Cluster cluster, List<Server> healthy);

    protected static List<Server> healthyServers(Cluster cluster) {
        List<Server> healthy = cluster.servers().stream()
                .filter(Server::isHealthy)
                .toList();

        if (healthy.isEmpty()) {
            throw new IllegalStateException("No healthy servers in cluster: " + cluster.name());
        }

        return healthy;
    }
}
