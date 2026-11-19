package com.jvmux.network.balancer.rand;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RandomBalancerTest {

    @Test
    void shouldReturnHealthyServersWithinCluster() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Server server3 = new Server("10.0.0.3", 8080, (byte) 1, true);
        Set<Server> healthyServers = Set.of(server1, server2, server3);

        Cluster cluster = new Cluster("random-cluster", healthyServers);
        RandomBalancer balancer = new RandomBalancer();

        for (int i = 0; i < 50; i++) {
            Server selected = balancer.select(cluster);
            assertNotNull(selected);
            assertTrue(healthyServers.contains(selected));
        }
    }

    @Test
    void shouldCoverAllHealthyServersOverMultipleInvocations() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Cluster cluster = new Cluster("random-cluster", Set.of(server1, server2));

        RandomBalancer balancer = new RandomBalancer();
        Set<Server> observed = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            observed.add(balancer.select(cluster));
        }

        assertEquals(2, observed.size(), "Random selection should hit all healthy servers over 100 iterations");
    }
}
