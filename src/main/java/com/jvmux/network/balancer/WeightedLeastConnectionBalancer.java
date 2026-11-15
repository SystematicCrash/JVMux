package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.Comparator;
import java.util.List;

public class WeightedLeastConnectionBalancer extends AbstractLoadBalancer {

    @Override
    protected Server selectFrom(Cluster cluster, List<Server> healthy) {
        return healthy.stream()
                .min(Comparator.comparingDouble(s ->
                        (double) s.getActiveConns() / Math.max(1, s.getWeight())
                ))
                .orElse(healthy.getFirst());
    }
}
