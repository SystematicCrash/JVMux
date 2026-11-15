package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.List;

public abstract class AbstractLoadBalancer implements LoadBalancer {

    @Override
    public final Server select(Cluster cluster, SelectionContext context) {
        List<Server> healthy = healthyServers(cluster);

        if (healthy.size() == 1) {
            return healthy.getFirst();
        }

        return selectFrom(cluster, healthy, context);
    }

    @Override
    public final Server select(Cluster cluster) {
        return select(cluster, SelectionContext.empty());
    }

    protected Server selectFrom(Cluster cluster, List<Server> healthy, SelectionContext context) {
        return selectFrom(cluster, healthy);
    }

    protected Server selectFrom(Cluster cluster, List<Server> healthy) {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " must override selectFrom(...)");
    }

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