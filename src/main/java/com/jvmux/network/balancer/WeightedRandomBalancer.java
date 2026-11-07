package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedRandomBalancer extends AbstractLoadBalancer {

    @Override
    protected Server selectFrom(Cluster cluster, List<Server> healthy) {
        int totalWeight = healthy.stream().mapToInt(Server::getWeight).sum();
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int cursor = 0;

        for (Server server : healthy) {
            cursor += server.getWeight();
            if (cursor >= random) {
                return server;
            }
        }

        return healthy.get(ThreadLocalRandom.current().nextInt(healthy.size()));
    }
}
