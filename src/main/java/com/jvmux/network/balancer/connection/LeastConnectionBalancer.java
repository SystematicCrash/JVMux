package com.jvmux.network.balancer.connection;

import com.jvmux.network.balancer.AbstractLoadBalancer;
import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.List;

public class LeastConnectionBalancer extends AbstractLoadBalancer {

    @Override
    protected Server selectFrom(Cluster cluster, List<Server> healthy) {
        Server best = null;
        int leastConnections = 0;

        for (Server server : healthy) {
            if (server.getActiveConns() < leastConnections)
                best = server;
        }

        return best;
    }
}
