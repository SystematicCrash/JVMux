package com.jvmux.network.balancer.connection;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LeastConnectionBalancerTest {

    @Test
    void shouldNotReturnNullWhenAllServersHaveZeroConnections() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Cluster cluster = new Cluster("test-cluster", Set.of(server1, server2));

        LeastConnectionBalancer balancer = new LeastConnectionBalancer();
        Server selected = balancer.select(cluster);

        assertNotNull(selected, "Selected server should not be null when all connections are zero");
    }

    @Test
    void shouldSelectServerWithLeastConnections() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Server server3 = new Server("10.0.0.3", 8080, (byte) 1, true);

        server1.incConns();
        server1.incConns(); // 2 conns
        server2.incConns(); // 1 conn
        // server3 has 0 conns

        Cluster cluster = new Cluster("test-cluster", Set.of(server1, server2, server3));

        LeastConnectionBalancer balancer = new LeastConnectionBalancer();
        Server selected = balancer.select(cluster);

        assertNotNull(selected);
        assertEquals(server3, selected, "Should select server with 0 active connections");
    }
}
