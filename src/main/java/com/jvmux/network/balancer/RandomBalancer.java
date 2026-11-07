package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomBalancer extends AbstractLoadBalancer {

    @Override
    protected Server selectFrom(Cluster cluster, List<Server> healthy) {
        int index = ThreadLocalRandom.current().nextInt(healthy.size());
        return healthy.get(index);
    }
}
