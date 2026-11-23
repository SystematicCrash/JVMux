package com.jvmux.network.balancer.rand;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WeightedRandomBalancerTest {

    @Test
    void shouldFavorHigherWeightedServersProportionally() {
        Server heavyServer = new Server("10.0.0.1", 8080, (byte) 9, true); // weight 9
        Server lightServer = new Server("10.0.0.2", 8080, (byte) 1, true); // weight 1

        Cluster cluster = new Cluster("weighted-random-cluster", Set.of(heavyServer, lightServer));
        WeightedRandomBalancer balancer = new WeightedRandomBalancer();

        Map<Server, Integer> selections = new HashMap<>();
        int iterations = 1000;

        for (int i = 0; i < iterations; i++) {
            Server selected = balancer.select(cluster);
            selections.put(selected, selections.getOrDefault(selected, 0) + 1);
        }

        int heavyCount = selections.getOrDefault(heavyServer, 0);
        int lightCount = selections.getOrDefault(lightServer, 0);

        assertTrue(heavyCount > lightCount * 4,
                "Heavy server (weight 9) should receive significantly more traffic than light server (weight 1)");
        assertTrue(lightCount > 0, "Light server should still receive some traffic");
    }

    @Test
    void shouldAlwaysReturnHealthyServer() {
        Server serverA = new Server("10.0.0.1", 8080, (byte) 3, true);
        Server serverB = new Server("10.0.0.2", 8080, (byte) 7, true);
        Set<Server> healthyServers = Set.of(serverA, serverB);

        Cluster cluster = new Cluster("weighted-random-cluster", healthyServers);
        WeightedRandomBalancer balancer = new WeightedRandomBalancer();

        for (int i = 0; i < 50; i++) {
            Server selected = balancer.select(cluster);
            assertNotNull(selected);
            assertTrue(healthyServers.contains(selected));
        }
    }
}
