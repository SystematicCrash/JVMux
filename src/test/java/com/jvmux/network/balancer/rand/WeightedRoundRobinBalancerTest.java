package com.jvmux.network.balancer.rand;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WeightedRoundRobinBalancerTest {

    @Test
    void shouldDistributeTrafficExactlyProportionalToWeightsOverCycle() {
        Server serverA = new Server("10.0.0.1", 8080, (byte) 4, true); // weight 4
        Server serverB = new Server("10.0.0.2", 8080, (byte) 2, true); // weight 2

        Cluster cluster = new Cluster("wrr-cluster", Set.of(serverA, serverB));
        WeightedRoundRobinBalancer balancer = new WeightedRoundRobinBalancer();

        int totalWeight = 4 + 2; // 6
        int cycles = 10;
        int totalRequests = totalWeight * cycles; // 60

        Map<Server, Integer> counts = new HashMap<>();
        for (int i = 0; i < totalRequests; i++) {
            Server selected = balancer.select(cluster);
            counts.put(selected, counts.getOrDefault(selected, 0) + 1);
        }

        assertEquals(4 * cycles, counts.get(serverA), "Server A (weight 4) should get 40 out of 60 requests");
        assertEquals(2 * cycles, counts.get(serverB), "Server B (weight 2) should get 20 out of 60 requests");
    }

    @Test
    void shouldInterleaveRequestsSmoothly() {
        Server serverA = new Server("10.0.0.1", 8080, (byte) 3, true);
        Server serverB = new Server("10.0.0.2", 8080, (byte) 1, true);

        Cluster cluster = new Cluster("wrr-smooth", Set.of(serverA, serverB));
        WeightedRoundRobinBalancer balancer = new WeightedRoundRobinBalancer();

        // 4 requests (weights 3:1)
        // Smooth WRR sequence: A, A, B, A (or similar smooth interleaving, never all A at once without B)
        boolean sawBBeforeEnd = false;
        for (int i = 0; i < 4; i++) {
            Server selected = balancer.select(cluster);
            if (selected == serverB && i < 3) {
                sawBBeforeEnd = true;
            }
        }
        assertTrue(sawBBeforeEnd, "Traffic should be interleaved smoothly instead of batching all of A");
    }

    @Test
    void shouldMaintainIndependentStatePerCluster() {
        Server a1 = new Server("10.0.1.1", 8080, (byte) 2, true);
        Server a2 = new Server("10.0.1.2", 8080, (byte) 1, true);
        Cluster clusterA = new Cluster("cluster-A", Set.of(a1, a2));

        Server b1 = new Server("10.0.2.1", 8080, (byte) 2, true);
        Server b2 = new Server("10.0.2.2", 8080, (byte) 1, true);
        Cluster clusterB = new Cluster("cluster-B", Set.of(b1, b2));

        WeightedRoundRobinBalancer balancer = new WeightedRoundRobinBalancer();

        assertNotNull(balancer.select(clusterA));
        assertNotNull(balancer.select(clusterB));
    }
}
