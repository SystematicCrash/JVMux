package com.jvmux.network.balancer.rand;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class RoundRobinBalancerTest {

    @Test
    void shouldCycleSequentiallyThroughServers() {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Server server3 = new Server("10.0.0.3", 8080, (byte) 1, true);

        // Keep order consistent for sequential checking
        List<Server> orderedServers = List.of(server1, server2, server3);
        Cluster cluster = new Cluster("rr-cluster", new LinkedHashSet<>(orderedServers));

        RoundRobinBalancer balancer = new RoundRobinBalancer();

        List<Server> firstCycle = List.of(
                balancer.select(cluster),
                balancer.select(cluster),
                balancer.select(cluster)
        );

        List<Server> secondCycle = List.of(
                balancer.select(cluster),
                balancer.select(cluster),
                balancer.select(cluster)
        );

        assertEquals(firstCycle, secondCycle, "Cycles should repeat in the exact same sequence");
        assertEquals(3, new HashSet<>(firstCycle).size(), "Each cycle must visit all 3 distinct servers");
    }

    @Test
    void shouldMaintainSeparatePointersPerCluster() {
        Server a1 = new Server("10.0.1.1", 8080, (byte) 1, true);
        Server a2 = new Server("10.0.1.2", 8080, (byte) 1, true);
        Cluster clusterA = new Cluster("cluster-A", Set.of(a1, a2));

        Server b1 = new Server("10.0.2.1", 8080, (byte) 1, true);
        Server b2 = new Server("10.0.2.2", 8080, (byte) 1, true);
        Cluster clusterB = new Cluster("cluster-B", Set.of(b1, b2));

        RoundRobinBalancer balancer = new RoundRobinBalancer();

        Server aFirst = balancer.select(clusterA);
        Server bFirst = balancer.select(clusterB);

        assertNotNull(aFirst);
        assertNotNull(bFirst);

        Server aSecond = balancer.select(clusterA);
        assertNotEquals(aFirst, aSecond, "Second call to cluster A must yield next server in cluster A");
    }

    @Test
    void shouldHandleConcurrentRequestsSafely() throws InterruptedException, ExecutionException {
        Server server1 = new Server("10.0.0.1", 8080, (byte) 1, true);
        Server server2 = new Server("10.0.0.2", 8080, (byte) 1, true);
        Cluster cluster = new Cluster("concurrent-cluster", Set.of(server1, server2));

        RoundRobinBalancer balancer = new RoundRobinBalancer();

        int threadCount = 10;
        int requestsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Callable<List<Server>>> tasks = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                List<Server> results = new ArrayList<>();
                for (int j = 0; j < requestsPerThread; j++) {
                    results.add(balancer.select(cluster));
                }
                return results;
            });
        }

        List<Future<List<Server>>> futures = executor.invokeAll(tasks);
        Map<Server, Integer> counts = new HashMap<>();

        for (Future<List<Server>> future : futures) {
            for (Server s : future.get()) {
                counts.put(s, counts.getOrDefault(s, 0) + 1);
            }
        }

        executor.shutdown();

        int totalRequests = threadCount * requestsPerThread;
        assertEquals(totalRequests / 2, counts.get(server1), "Traffic should be evenly split between server1 and server2");
        assertEquals(totalRequests / 2, counts.get(server2), "Traffic should be evenly split between server1 and server2");
    }
}
