package com.jvmux.network.balancer.hash;

import com.jvmux.common.util.XxHasher;
import com.jvmux.network.balancer.AbstractLoadBalancer;
import com.jvmux.network.balancer.SelectionContext;
import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.Comparator;
import java.util.List;

public class IpHashBalancer extends AbstractLoadBalancer {

    @Override
    protected Server selectFrom(Cluster cluster, List<Server> healthy, SelectionContext context) {
        List<Server> sorted = healthy.stream()
                .sorted(Comparator.comparing(Server::getHost).thenComparingInt(Server::getPort))
                .toList();

        if (context == null || context.clientIp() == null) {
            throw new IllegalArgumentException("ClientIp cannot be null");
        }

        int hash = XxHasher.hash32(context.clientIp()) & 0x7FFFFFFF;
        int index = hash % sorted.size();

        return sorted.get(index);
    }
}