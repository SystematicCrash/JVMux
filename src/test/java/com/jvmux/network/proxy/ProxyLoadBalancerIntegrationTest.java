package com.jvmux.network.proxy;

import com.jvmux.network.balancer.BalancingStrategy;
import com.jvmux.network.balancer.LoadBalancer;
import com.jvmux.network.balancer.LoadBalancerFactory;
import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.model.Client;
import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import com.jvmux.network.routing.RouteTree;
import com.jvmux.network.routing.Router;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ProxyLoadBalancerIntegrationTest {

    private ExecutorService proxyExecutor;
    private final List<MockUpstreamServer> mockServers = new ArrayList<>();
    private ServerSocket proxyListener;
    private int proxyPort;
    private final AtomicBoolean proxyRunning = new AtomicBoolean(false);

    @BeforeEach
    void setUp() throws IOException {
        proxyExecutor = Executors.newVirtualThreadPerTaskExecutor();
        proxyListener = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        proxyPort = proxyListener.getLocalPort();
        proxyRunning.set(true);
    }

    @AfterEach
    void tearDown() throws IOException {
        proxyRunning.set(false);
        if (proxyListener != null && !proxyListener.isClosed()) {
            proxyListener.close();
        }
        for (MockUpstreamServer s : mockServers) {
            s.close();
        }
        mockServers.clear();
        if (proxyExecutor != null) {
            proxyExecutor.shutdownNow();
        }
    }

    private void startProxy(RouteTree routeTree, LoadBalancer balancer) {
        proxyExecutor.submit(() -> {
            while (proxyRunning.get() && !proxyListener.isClosed()) {
                try {
                    Socket clientSocket = proxyListener.accept();
                    proxyExecutor.submit(() -> {
                        try (var proxy = new ProxyRelay(clientSocket, routeTree, balancer)) {
                            proxy.run();
                        } catch (Exception ignored) {}
                    });
                } catch (IOException e) {
                    if (!proxyRunning.get()) break;
                }
            }
        });
    }

    private String sendRequest(String path) throws IOException {
        return sendRequest(path, "127.0.0.1", null);
    }

    private String sendRequest(String path, String clientBindIp, Map<String, String> extraHeaders) throws IOException {
        try (Socket socket = new Socket()) {
            if (clientBindIp != null) {
                socket.bind(new InetSocketAddress(InetAddress.getByName(clientBindIp), 0));
            }
            socket.connect(new InetSocketAddress("127.0.0.1", proxyPort), 3000);
            socket.setSoTimeout(3000);

            OutputStream out = socket.getOutputStream();
            StringBuilder req = new StringBuilder();
            req.append("GET ").append(path).append(" HTTP/1.1\r\n");
            req.append("Host: localhost\r\n");
            req.append("Connection: close\r\n");
            if (extraHeaders != null) {
                for (var entry : extraHeaders.entrySet()) {
                    req.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
                }
            }
            req.append("\r\n");

            out.write(req.toString().getBytes(StandardCharsets.US_ASCII));
            out.flush();

            InputStream in = socket.getInputStream();
            return new String(in.readAllBytes(), StandardCharsets.US_ASCII);
        }
    }

    private String extractHeader(String httpResponse, String headerName) {
        String prefix = headerName.toLowerCase() + ":";
        for (String line : httpResponse.split("\r\n")) {
            if (line.toLowerCase().startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return null;
    }

    @Test
    void testRoundRobinThroughProxy() throws Exception {
        MockUpstreamServer s1 = new MockUpstreamServer("server-1");
        MockUpstreamServer s2 = new MockUpstreamServer("server-2");
        MockUpstreamServer s3 = new MockUpstreamServer("server-3");
        mockServers.addAll(List.of(s1, s2, s3));

        Server server1 = new Server(s1.getHost(), s1.getPort(), (byte) 1, true);
        Server server2 = new Server(s2.getHost(), s2.getPort(), (byte) 1, true);
        Server server3 = new Server(s3.getHost(), s3.getPort(), (byte) 1, true);

        // Keep insertion order predictable
        Cluster cluster = new Cluster("rr-cluster", new LinkedHashSet<>(List.of(server1, server2, server3)));
        Router router = new Router();
        router.add("/service", cluster, HttpMethod.GET);

        LoadBalancer balancer = LoadBalancerFactory.create(BalancingStrategy.ROUND_ROBIN);
        startProxy(router.build(), balancer);

        List<String> visitedServers = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            String response = sendRequest("/service");
            assertTrue(response.startsWith("HTTP/1.1 200 OK"));
            String serverId = extractHeader(response, "X-Server-Id");
            assertNotNull(serverId);
            visitedServers.add(serverId);
        }

        // Each server should be hit exactly twice in 2 full cycles
        assertEquals(2, Collections.frequency(visitedServers, "server-1"));
        assertEquals(2, Collections.frequency(visitedServers, "server-2"));
        assertEquals(2, Collections.frequency(visitedServers, "server-3"));

        // Sequence must cycle cleanly
        List<String> firstCycle = visitedServers.subList(0, 3);
        List<String> secondCycle = visitedServers.subList(3, 6);
        assertEquals(firstCycle, secondCycle, "Cycles should repeat in the exact same sequence");
    }

    @Test
    void testWeightedRoundRobinThroughProxy() throws Exception {
        MockUpstreamServer sHeavy = new MockUpstreamServer("heavy");
        MockUpstreamServer sLight = new MockUpstreamServer("light");
        mockServers.addAll(List.of(sHeavy, sLight));

        Server serverHeavy = new Server(sHeavy.getHost(), sHeavy.getPort(), (byte) 3, true); // weight 3
        Server serverLight = new Server(sLight.getHost(), sLight.getPort(), (byte) 1, true); // weight 1

        Cluster cluster = new Cluster("wrr-cluster", Set.of(serverHeavy, serverLight));
        Router router = new Router();
        router.add("/wrr", cluster, HttpMethod.GET);

        LoadBalancer balancer = LoadBalancerFactory.create(BalancingStrategy.WEIGHTED_ROUND_ROBIN);
        startProxy(router.build(), balancer);

        int totalRequests = 8; // exactly 2 cycles of (3 + 1)
        List<String> responses = new ArrayList<>();
        for (int i = 0; i < totalRequests; i++) {
            String response = sendRequest("/wrr");
            assertTrue(response.startsWith("HTTP/1.1 200 OK"));
            responses.add(extractHeader(response, "X-Server-Id"));
        }

        assertEquals(6, Collections.frequency(responses, "heavy"), "Heavy server (weight 3) should receive 6 of 8 requests");
        assertEquals(2, Collections.frequency(responses, "light"), "Light server (weight 1) should receive 2 of 8 requests");
    }

    @Test
    void testLeastConnectionsThroughProxy() throws Exception {
        MockUpstreamServer s1 = new MockUpstreamServer("server-1");
        MockUpstreamServer s2 = new MockUpstreamServer("server-2");
        mockServers.addAll(List.of(s1, s2));

        Server server1 = new Server(s1.getHost(), s1.getPort(), (byte) 1, true);
        Server server2 = new Server(s2.getHost(), s2.getPort(), (byte) 1, true);

        // Pre-load server1 with active connections
        server1.incConns();
        server1.incConns(); // 2 active conns

        Cluster cluster = new Cluster("least-conn-cluster", Set.of(server1, server2));
        Router router = new Router();
        router.add("/least", cluster, HttpMethod.GET);

        LoadBalancer balancer = LoadBalancerFactory.create(BalancingStrategy.LEAST_CONNECTIONS);
        startProxy(router.build(), balancer);

        // When server1 has 2 active connections and server2 has 0, server2 must be chosen
        String response = sendRequest("/least");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertEquals("server-2", extractHeader(response, "X-Server-Id"));
    }

    @Test
    void testWeightedLeastConnectionsThroughProxy() throws Exception {
        MockUpstreamServer sA = new MockUpstreamServer("server-A");
        MockUpstreamServer sB = new MockUpstreamServer("server-B");
        mockServers.addAll(List.of(sA, sB));

        // server-A: weight 2, activeConns 2 -> ratio = 1.0
        Server serverA = new Server(sA.getHost(), sA.getPort(), (byte) 2, true);
        serverA.incConns();
        serverA.incConns();

        // server-B: weight 10, activeConns 3 -> ratio = 0.3
        Server serverB = new Server(sB.getHost(), sB.getPort(), (byte) 10, true);
        serverB.incConns();
        serverB.incConns();
        serverB.incConns();

        Cluster cluster = new Cluster("wlc-cluster", Set.of(serverA, serverB));
        Router router = new Router();
        router.add("/wlc", cluster, HttpMethod.GET);

        LoadBalancer balancer = LoadBalancerFactory.create(BalancingStrategy.WEIGHTED_LEAST_CONNECTIONS);
        startProxy(router.build(), balancer);

        String response = sendRequest("/wlc");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertEquals("server-B", extractHeader(response, "X-Server-Id"),
                "Server B should be chosen because 3/10 (0.3) < 2/2 (1.0)");
    }

    @Test
    void testPowerOfTwoChoicesThroughProxy() throws Exception {
        MockUpstreamServer sGood1 = new MockUpstreamServer("good-1");
        MockUpstreamServer sGood2 = new MockUpstreamServer("good-2");
        MockUpstreamServer sBad = new MockUpstreamServer("overloaded");
        mockServers.addAll(List.of(sGood1, sGood2, sBad));

        Server serverGood1 = new Server(sGood1.getHost(), sGood1.getPort(), (byte) 1, true);
        Server serverGood2 = new Server(sGood2.getHost(), sGood2.getPort(), (byte) 1, true);
        Server serverBad = new Server(sBad.getHost(), sBad.getPort(), (byte) 1, true);

        // Simulate 100 active connections on the overloaded server
        for (int i = 0; i < 100; i++) serverBad.incConns();

        Cluster cluster = new Cluster("p2c-cluster", Set.of(serverGood1, serverGood2, serverBad));
        Router router = new Router();
        router.add("/p2c", cluster, HttpMethod.GET);

        LoadBalancer balancer = LoadBalancerFactory.create(BalancingStrategy.POWER_OF_TWO_CHOICES);
        startProxy(router.build(), balancer);

        for (int i = 0; i < 20; i++) {
            String response = sendRequest("/p2c");
            assertTrue(response.startsWith("HTTP/1.1 200 OK"));
            String id = extractHeader(response, "X-Server-Id");
            assertNotEquals("overloaded", id, "Overloaded node should not win in P2C competition");
        }
    }

    @Test
    void testIpHashAffinityThroughProxy() throws Exception {
        MockUpstreamServer s1 = new MockUpstreamServer("server-1");
        MockUpstreamServer s2 = new MockUpstreamServer("server-2");
        mockServers.addAll(List.of(s1, s2));

        Server server1 = new Server(s1.getHost(), s1.getPort(), (byte) 1, true);
        Server server2 = new Server(s2.getHost(), s2.getPort(), (byte) 1, true);

        Cluster cluster = new Cluster("ip-hash-cluster", Set.of(server1, server2));
        Router router = new Router();
        router.add("/iphash", cluster, HttpMethod.GET);

        LoadBalancer balancer = LoadBalancerFactory.create(BalancingStrategy.IP_HASH);
        startProxy(router.build(), balancer);

        // Requests originating from 127.0.0.1
        String response1 = sendRequest("/iphash", "127.0.0.1", null);
        String target1 = extractHeader(response1, "X-Server-Id");

        for (int i = 0; i < 5; i++) {
            String response = sendRequest("/iphash", "127.0.0.1", null);
            assertEquals(target1, extractHeader(response, "X-Server-Id"), "Same client IP must always hit the same upstream node");
        }

        // Requests originating from 127.0.0.2
        String response2 = sendRequest("/iphash", "127.0.0.2", null);
        String target2 = extractHeader(response2, "X-Server-Id");

        for (int i = 0; i < 5; i++) {
            String response = sendRequest("/iphash", "127.0.0.2", null);
            assertEquals(target2, extractHeader(response, "X-Server-Id"), "Same client IP must always hit the same upstream node");
        }
    }

    @Test
    void testConsistentHashAffinityAndFailover() throws Exception {
        MockUpstreamServer s1 = new MockUpstreamServer("server-1");
        MockUpstreamServer s2 = new MockUpstreamServer("server-2");
        mockServers.addAll(List.of(s1, s2));

        Server server1 = new Server(s1.getHost(), s1.getPort(), (byte) 1, true);
        Server server2 = new Server(s2.getHost(), s2.getPort(), (byte) 1, true);

        Cluster cluster = new Cluster("chash-cluster", Set.of(server1, server2));
        Router router = new Router();
        router.add("/chash", cluster, HttpMethod.GET);

        LoadBalancer balancer = LoadBalancerFactory.create(BalancingStrategy.CONSISTENT_HASH);
        startProxy(router.build(), balancer);

        // Consistent routing for client IP 127.0.0.1
        String initial = sendRequest("/chash", "127.0.0.1", null);
        String initialTarget = extractHeader(initial, "X-Server-Id");

        for (int i = 0; i < 5; i++) {
            String response = sendRequest("/chash", "127.0.0.1", null);
            assertEquals(initialTarget, extractHeader(response, "X-Server-Id"));
        }

        // When the selected server becomes unhealthy, ring seamlessly shifts traffic
        Server active = initialTarget.equals("server-1") ? server1 : server2;
        active.setHealthy(false);

        String failoverResponse = sendRequest("/chash", "127.0.0.1", null);
        assertTrue(failoverResponse.startsWith("HTTP/1.1 200 OK"));
        String failoverTarget = extractHeader(failoverResponse, "X-Server-Id");
        assertNotEquals(initialTarget, failoverTarget, "Traffic must failover to the healthy server on the ring");
    }

    @Test
    void testPeakEwmaLatencyAwareRouting() throws Exception {
        MockUpstreamServer sFast = new MockUpstreamServer("fast-server");
        MockUpstreamServer sSlow = new MockUpstreamServer("slow-server");
        mockServers.addAll(List.of(sFast, sSlow));

        Server serverFast = new Server(sFast.getHost(), sFast.getPort(), (byte) 1, true);
        Server serverSlow = new Server(sSlow.getHost(), sSlow.getPort(), (byte) 1, true);

        // Record EWMA response latencies: Fast = 5ms, Slow = 100ms
        serverFast.recordResponseTime(5_000_000L);
        serverSlow.recordResponseTime(100_000_000L);

        Cluster cluster = new Cluster("ewma-cluster", Set.of(serverFast, serverSlow));
        Router router = new Router();
        router.add("/ewma", cluster, HttpMethod.GET);

        LoadBalancer balancer = LoadBalancerFactory.create(BalancingStrategy.PEAK_EWMA);
        startProxy(router.build(), balancer);

        for (int i = 0; i < 5; i++) {
            String response = sendRequest("/ewma");
            assertTrue(response.startsWith("HTTP/1.1 200 OK"));
            assertEquals("fast-server", extractHeader(response, "X-Server-Id"));
        }
    }

    @Test
    void testRandomAndWeightedRandomThroughProxy() throws Exception {
        MockUpstreamServer sHeavy = new MockUpstreamServer("heavy");
        MockUpstreamServer sLight = new MockUpstreamServer("light");
        mockServers.addAll(List.of(sHeavy, sLight));

        Server serverHeavy = new Server(sHeavy.getHost(), sHeavy.getPort(), (byte) 9, true); // weight 9
        Server serverLight = new Server(sLight.getHost(), sLight.getPort(), (byte) 1, true); // weight 1

        Cluster cluster = new Cluster("wrand-cluster", Set.of(serverHeavy, serverLight));
        Router router = new Router();
        router.add("/wrand", cluster, HttpMethod.GET);

        LoadBalancer balancer = LoadBalancerFactory.create(BalancingStrategy.WEIGHTED_RANDOM);
        startProxy(router.build(), balancer);

        int heavyCount = 0;
        int lightCount = 0;
        for (int i = 0; i < 100; i++) {
            String response = sendRequest("/wrand");
            String id = extractHeader(response, "X-Server-Id");
            if ("heavy".equals(id)) heavyCount++;
            else if ("light".equals(id)) lightCount++;
        }

        assertTrue(heavyCount > lightCount * 3, "Heavy server (weight 9) should receive substantially more requests than light server (weight 1)");
        assertTrue(lightCount > 0, "Light server should still receive some requests");
    }

    @Test
    void testUpstreamFailoverWithLoadBalancerRetry() throws Exception {
        MockUpstreamServer brokenServer = new MockUpstreamServer("broken");
        brokenServer.setShouldFail(true); // Fails connection immediately

        MockUpstreamServer healthyServer = new MockUpstreamServer("healthy");
        mockServers.addAll(List.of(brokenServer, healthyServer));

        Server serverBroken = new Server(brokenServer.getHost(), brokenServer.getPort(), (byte) 1, true);
        Server serverHealthy = new Server(healthyServer.getHost(), healthyServer.getPort(), (byte) 1, true);

        // Put broken server first in round robin
        Cluster cluster = new Cluster("failover-cluster", new LinkedHashSet<>(List.of(serverBroken, serverHealthy)));
        Router router = new Router();
        router.add("/failover", cluster, HttpMethod.GET);

        LoadBalancer balancer = LoadBalancerFactory.create(BalancingStrategy.ROUND_ROBIN);
        startProxy(router.build(), balancer);

        // Client request hits broken server first, UpstreamHandler retries with healthy server
        String response = sendRequest("/failover");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertEquals("healthy", extractHeader(response, "X-Server-Id"), "Proxy retry must failover to the healthy server");
    }

    @Test
    void testAllServersUnhealthyReturns502BadGateway() throws Exception {
        MockUpstreamServer s1 = new MockUpstreamServer("server-1");
        mockServers.add(s1);

        Server server1 = new Server(s1.getHost(), s1.getPort(), (byte) 1, false); // Unhealthy

        Cluster cluster = new Cluster("down-cluster", Set.of(server1));
        Router router = new Router();
        router.add("/down", cluster, HttpMethod.GET);

        LoadBalancer balancer = LoadBalancerFactory.create(BalancingStrategy.ROUND_ROBIN);
        startProxy(router.build(), balancer);

        String response = sendRequest("/down");
        assertTrue(response.startsWith("HTTP/1.1 502 Bad Gateway"), "When all servers are down, proxy must return 502 Bad Gateway");
    }

    @Test
    void testServerConnectionCounterLifecycle() throws Exception {
        MockUpstreamServer s1 = new MockUpstreamServer("server-1");
        mockServers.add(s1);

        Server server1 = new Server(s1.getHost(), s1.getPort(), (byte) 1, true);

        assertEquals(0, server1.getActiveConns(), "Connections should start at 0");

        Cluster cluster = new Cluster("conns-cluster", Set.of(server1));
        Router router = new Router();
        router.add("/conns", cluster, HttpMethod.GET);

        LoadBalancer balancer = LoadBalancerFactory.create(BalancingStrategy.ROUND_ROBIN);
        startProxy(router.build(), balancer);

        String response = sendRequest("/conns");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));

        // Give proxy a moment to complete close()
        Thread.sleep(100);

        assertEquals(0, server1.getActiveConns(), "Connections must decrement back to 0 after proxy request completes");
    }
}
