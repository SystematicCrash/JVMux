package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinBalancer extends AbstractLoadBalancer {

    private final ConcurrentHashMap<String, AtomicInteger> pointers = new ConcurrentHashMap<>();

    @Override
    protected Server selectFrom(Cluster cluster, List<Server> healthy) {
        int size = healthy.size();

        AtomicInteger counter = pointers.computeIfAbsent(cluster.name(), k -> new AtomicInteger(0));

        int index = counter.getAndUpdate(cur -> (cur + 1) % size);
        return healthy.get(index);
    }
}
