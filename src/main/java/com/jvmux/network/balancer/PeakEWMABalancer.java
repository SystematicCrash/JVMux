package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.Comparator;
import java.util.List;

public class PeakEWMABalancer extends AbstractLoadBalancer {

    @Override
    protected Server selectFrom(Cluster cluster, List<Server> healthy) {
        return healthy.stream()
                .min(Comparator.comparingLong(s -> s.getResponseTimeEwmaNanos().get()))
                .orElse(healthy.getFirst());
    }
}
