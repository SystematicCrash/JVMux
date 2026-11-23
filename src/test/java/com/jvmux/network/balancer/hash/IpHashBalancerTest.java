package com.jvmux.network.balancer.hash;

import com.jvmux.network.balancer.SelectionContext;
import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IpHashBalancerTest {

    @Test
    void shouldThrowIllegalArgumentExceptionWhenContextOrClientIpIsNull() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Cluster cluster = new Cluster("ip-hash-cluster", Set.of(server1, server2));

        IpHashBalancer balancer = new IpHashBalancer();

        assertThrows(IllegalArgumentException.class, () -> balancer.select(cluster, null));
        assertThrows(IllegalArgumentException.class, () -> balancer.select(cluster, SelectionContext.empty()));
    }

    @Test
    void shouldConsistentlyRouteSameClientIpToSameServer() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Server server3 = new Server("10.0.0.3", 8080, (byte) 1, true);
        Cluster cluster = new Cluster("ip-hash-cluster", Set.of(server1, server2, server3));

        IpHashBalancer balancer = new IpHashBalancer();
        SelectionContext context = new SelectionContext("192.168.1.1");

        Server expected = balancer.select(cluster, context);
        for (int i = 0; i < 50; i++) {
            assertEquals(expected, balancer.select(cluster, context),
                    "IpHashBalancer must deterministically route the same client IP to the same server");
        }
    }

    @Test
    void shouldDistributeDifferentIpsAcrossServers() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Cluster cluster = new Cluster("ip-hash-cluster", Set.of(server1, server2));

        IpHashBalancer balancer = new IpHashBalancer();
        Set<Server> chosen = new HashSet<>();

        for (int i = 1; i <= 100; i++) {
            chosen.add(balancer.select(cluster, new SelectionContext("10.0.0." + i)));
        }

        assertEquals(2, chosen.size(), "Both servers should be selected across diverse IPs");
    }
}
