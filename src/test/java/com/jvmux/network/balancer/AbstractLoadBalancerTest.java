package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AbstractLoadBalancerTest {

    static class TestBalancer extends AbstractLoadBalancer {
        final AtomicBoolean selectFromCalled = new AtomicBoolean(false);

        @Override
        protected Server selectFrom(Cluster cluster, List<Server> healthy, SelectionContext context) {
            selectFromCalled.set(true);
            return healthy.getFirst();
        }
    }

    @Test
    void shouldReturnSingleServerImmediatelyWithoutCallingSelectFrom() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, false); // unhealthy
        Cluster cluster = new Cluster("single-healthy-cluster", Set.of(server1, server2));

        TestBalancer balancer = new TestBalancer();
        Server selected = balancer.select(cluster);

        assertEquals(server1, selected);
        assertFalse(balancer.selectFromCalled.get(), "selectFrom should not be called when exactly one server is healthy");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenNoHealthyServersInCluster() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, false);
        Cluster cluster = new Cluster("unhealthy-cluster", Set.of(server1));

        TestBalancer balancer = new TestBalancer();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> balancer.select(cluster));
        assertTrue(ex.getMessage().contains("No healthy servers in cluster: unhealthy-cluster"));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenClusterIsEmpty() {
        Cluster cluster = new Cluster("empty-cluster", Collections.emptySet());

        TestBalancer balancer = new TestBalancer();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> balancer.select(cluster));
        assertTrue(ex.getMessage().contains("No healthy servers in cluster: empty-cluster"));
    }

    @Test
    void shouldDelegateToSelectFromWhenMultipleHealthyServers() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Cluster cluster = new Cluster("multi-cluster", Set.of(server1, server2));

        TestBalancer balancer = new TestBalancer();
        Server selected = balancer.select(cluster);

        assertNotNull(selected);
        assertTrue(balancer.selectFromCalled.get());
    }

    @Test
    void shouldThrowUnsupportedOperationExceptionIfSelectFromNotOverridden() {
        AbstractLoadBalancer incompleteBalancer = new AbstractLoadBalancer() {};

        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Cluster cluster = new Cluster("multi-cluster", Set.of(server1, server2));

        assertThrows(UnsupportedOperationException.class, () -> incompleteBalancer.select(cluster));
    }
}
