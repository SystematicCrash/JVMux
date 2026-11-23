package com.jvmux.network.balancer.connection;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WeightedLeastConnectionBalancerTest {

    @Test
    void shouldSelectServerWithLowestConnectionsToWeightRatio() {
        // Server A: weight 2, activeConns 4 -> ratio = 4 / 2 = 2.0
        Server serverA = new Server("10.0.0.1", 8080, (byte) 2, true);
        for (int i = 0; i < 4; i++) serverA.incConns();

        // Server B: weight 10, activeConns 5 -> ratio = 5 / 10 = 0.5
        Server serverB = new Server("10.0.0.2", 8080, (byte) 10, true);
        for (int i = 0; i < 5; i++) serverB.incConns();

        Cluster cluster = new Cluster("weighted-cluster", Set.of(serverA, serverB));

        WeightedLeastConnectionBalancer balancer = new WeightedLeastConnectionBalancer();
        Server selected = balancer.select(cluster);

        assertEquals(serverB, selected, "Server B should be chosen because 5/10 (0.5) < 4/2 (2.0)");
    }

    @Test
    void shouldReturnNonNullableServerWhenAllConnectionsAreZero() {
        Server serverA = new Server("10.0.0.1", 8080, (byte) 2, true);
        Server serverB = new Server("10.0.0.2", 8080, (byte) 5, true);

        Cluster cluster = new Cluster("weighted-cluster", Set.of(serverA, serverB));

        WeightedLeastConnectionBalancer balancer = new WeightedLeastConnectionBalancer();
        Server selected = balancer.select(cluster);

        assertNotNull(selected);
        assertTrue(selected == serverA || selected == serverB);
    }

    @Test
    void shouldAdaptWhenConnectionsChange() {
        Server serverA = new Server("10.0.0.1", 8080, (byte) 5, true);
        Server serverB = new Server("10.0.0.2", 8080, (byte) 5, true);

        serverA.incConns(); // A: 1/5 = 0.2, B: 0/5 = 0.0
        Cluster cluster = new Cluster("weighted-cluster", Set.of(serverA, serverB));

        WeightedLeastConnectionBalancer balancer = new WeightedLeastConnectionBalancer();
        assertEquals(serverB, balancer.select(cluster));

        serverB.incConns();
        serverB.incConns(); // B: 2/5 = 0.4, A: 1/5 = 0.2
        assertEquals(serverA, balancer.select(cluster));
    }
}
