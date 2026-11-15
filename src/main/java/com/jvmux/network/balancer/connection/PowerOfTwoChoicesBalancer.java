package com.jvmux.network.balancer.connection;

import com.jvmux.network.balancer.AbstractLoadBalancer;
import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PowerOfTwoChoicesBalancer extends AbstractLoadBalancer {

    @Override
    protected Server selectFrom(Cluster cluster, List<Server> healthy) {
        var random = ThreadLocalRandom.current();

        Server first = healthy.get(random.nextInt(healthy.size()));
        Server second = healthy.get(random.nextInt(healthy.size()));

        while (second == first) {
            second = healthy.get(random.nextInt(healthy.size()));
        }

        return first.getActiveConns() <= second.getActiveConns() ? first : second;
    }
}
