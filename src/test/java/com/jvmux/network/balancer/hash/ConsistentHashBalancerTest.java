package com.jvmux.network.balancer.hash;

import com.jvmux.network.balancer.SelectionContext;
import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConsistentHashBalancerTest {

    @Test
    void shouldThrowIllegalArgumentExceptionWhenContextOrIpIsNull() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Cluster cluster = new Cluster("hash-cluster", Set.of(server1, server2));

        ConsistentHashBalancer balancer = new ConsistentHashBalancer();

        assertThrows(IllegalArgumentException.class, () -> balancer.select(cluster, null));
        assertThrows(IllegalArgumentException.class, () -> balancer.select(cluster, SelectionContext.empty()));
    }

    @Test
    void shouldConsistentlyRouteSameClientIpToSameServer() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Server server3 = new Server("10.0.0.3", 8080, (byte) 1, true);
        Cluster cluster = new Cluster("hash-cluster", Set.of(server1, server2, server3));

        ConsistentHashBalancer balancer = new ConsistentHashBalancer();

        SelectionContext clientA = new SelectionContext("192.168.1.50");
        Server initialChoiceA = balancer.select(cluster, clientA);

        for (int i = 0; i < 50; i++) {
            assertEquals(initialChoiceA, balancer.select(cluster, clientA),
                    "Same client IP must always hash to the same server");
        }

        SelectionContext clientB = new SelectionContext("10.200.0.99");
        Server initialChoiceB = balancer.select(cluster, clientB);

        for (int i = 0; i < 50; i++) {
            assertEquals(initialChoiceB, balancer.select(cluster, clientB),
                    "Same client IP must always hash to the same server");
        }
    }

    @Test
    void shouldDistributeMultipleClientsAcrossServers() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Cluster cluster = new Cluster("hash-cluster", Set.of(server1, server2));

        ConsistentHashBalancer balancer = new ConsistentHashBalancer();

        Set<Server> selectedServers = new HashSet<>();
        for (int i = 1; i <= 100; i++) {
            SelectionContext context = new SelectionContext("192.168.0." + i);
            selectedServers.add(balancer.select(cluster, context));
        }

        assertEquals(2, selectedServers.size(), "Both servers should receive traffic across 100 distinct client IPs");
    }

    @Test
    void shouldAdaptWhenClusterMembersChange() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Cluster clusterBefore = new Cluster("hash-cluster", Set.of(server1, server2));

        ConsistentHashBalancer balancer = new ConsistentHashBalancer();
        SelectionContext context = new SelectionContext("172.16.0.10");

        Server before = balancer.select(clusterBefore, context);
        assertNotNull(before);

        // Add a third server
        Server server3 = new Server("10.0.0.3", 8080, (byte) 1, true);
        Cluster clusterAfter = new Cluster("hash-cluster", Set.of(server1, server2, server3));

        Server after = balancer.select(clusterAfter, context);
        assertNotNull(after);
    }
}
