package com.jvmux.network.balancer.latency;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PeakEWMABalancerTest {

    @Test
    void shouldSelectServerWithLowestEwmaLatency() {
        Server fastServer = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server slowServer = new Server("10.0.0.2", 8080, (byte) 1, true);

        // Record response times: 10ms vs 100ms in nanoseconds
        fastServer.recordResponseTime(10_000_000L);
        slowServer.recordResponseTime(100_000_000L);

        Cluster cluster = new Cluster("latency-cluster", Set.of(fastServer, slowServer));

        PeakEWMABalancer balancer = new PeakEWMABalancer();
        Server selected = balancer.select(cluster);

        assertEquals(fastServer, selected, "PeakEWMABalancer should select the server with lowest latency EWMA");
    }

    @Test
    void shouldAdaptWhenLatencyEwmaChanges() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);

        server1.recordResponseTime(50_000_000L);
        server2.recordResponseTime(20_000_000L);

        Cluster cluster = new Cluster("latency-cluster", Set.of(server1, server2));
        PeakEWMABalancer balancer = new PeakEWMABalancer();

        assertEquals(server2, balancer.select(cluster));

        // Now server2 degrades significantly
        for (int i = 0; i < 10; i++) {
            server2.recordResponseTime(200_000_000L);
        }

        assertEquals(server1, balancer.select(cluster));
    }
}
