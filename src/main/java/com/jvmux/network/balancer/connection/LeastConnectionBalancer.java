package com.jvmux.network.balancer.connection;

import com.jvmux.network.balancer.AbstractLoadBalancer;
import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.List;

public class LeastConnectionBalancer extends AbstractLoadBalancer {

    @Override
    protected Server selectFrom(Cluster cluster, List<Server> healthy) {
        Server best = healthy.getFirst();
        int leastConnections = best.getActiveConns();

        for (Server server : healthy) {
            int conns = server.getActiveConns();
            if (conns < leastConnections) {
                best = server;
                leastConnections = conns;
            }
        }

        return best;
    }
}
