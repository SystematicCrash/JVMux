package com.jvmux.network.balancer.connection;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PowerOfTwoChoicesBalancerTest {

    @Test
    void shouldSelectServerWithFewerConnectionsWhenOnlyTwoServersExist() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);

        server1.incConns(); // 1 conn
        // server2 has 0 conns

        Cluster cluster = new Cluster("p2c-cluster", Set.of(server1, server2));

        PowerOfTwoChoicesBalancer balancer = new PowerOfTwoChoicesBalancer();

        // When only two servers exist, both must be picked and server2 must always win
        for (int i = 0; i < 20; i++) {
            Server selected = balancer.select(cluster);
            assertEquals(server2, selected, "Server with fewer connections must always be selected");
        }
    }

    @Test
    void shouldNeverSelectOverloadedServerWhenBestServerIsAlwaysBetterInTwoChoices() {
        Server best = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server medium = new Server("10.0.0.2", 8080, (byte) 1, true);
        Server worst = new Server("10.0.0.3", 8080, (byte) 1, true);

        // best: 0 conns, medium: 5 conns, worst: 100 conns
        for (int i = 0; i < 5; i++) medium.incConns();
        for (int i = 0; i < 100; i++) worst.incConns();

        Cluster cluster = new Cluster("p2c-cluster", Set.of(best, medium, worst));
        PowerOfTwoChoicesBalancer balancer = new PowerOfTwoChoicesBalancer();

        for (int i = 0; i < 50; i++) {
            Server selected = balancer.select(cluster);
            assertNotEquals(worst, selected, "Worst server should not be selected when competing against better choices");
        }
    }

    @Test
    void shouldAlwaysReturnHealthyServer() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Server server3 = new Server("10.0.0.3", 8080, (byte) 1, true);

        Cluster cluster = new Cluster("p2c-cluster", Set.of(server1, server2, server3));
        PowerOfTwoChoicesBalancer balancer = new PowerOfTwoChoicesBalancer();

        Set<Server> healthy = Set.of(server1, server2, server3);
        for (int i = 0; i < 100; i++) {
            Server selected = balancer.select(cluster);
            assertNotNull(selected);
            assertTrue(healthy.contains(selected));
        }
    }
}
