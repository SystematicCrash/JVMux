package com.jvmux.network.proxy;

import com.jvmux.network.balancer.BalancingStrategy;
import com.jvmux.network.balancer.LoadBalancer;
import com.jvmux.network.balancer.SelectionContext;
import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.exception.HttpException;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.model.Client;
import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import com.jvmux.network.routing.Router;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class UpstreamHandlerTest {

    @Test
    void shouldBalanceConnectionUsingConfiguredLoadBalancer() throws Exception {
        AtomicBoolean balancerCalled = new AtomicBoolean(false);
        AtomicReference<SelectionContext> capturedContext = new AtomicReference<>();

        Server dummyServer = new Server("127.0.0.1", 59999, (byte) 1, true);
        Cluster testCluster = new Cluster("test-cluster", Set.of(dummyServer));

        LoadBalancer mockBalancer = (cluster, context) -> {
            balancerCalled.set(true);
            capturedContext.set(context);
            return dummyServer;
        };

        Router router = new Router();
        router.add("/api", testCluster, HttpMethod.GET);

        Client client = new Client("192.168.1.100", 54321, 8080);
        UpstreamHandler handler = new UpstreamHandler(router.build(), client, mockBalancer);

        HttpRequest request = HttpRequest.builder()
                .path("/api")
                .method(HttpMethod.GET)
                .version(HttpVersion.HTTP_1_1)
                .headers(new HashMap<>())
                .build();

        // Connection will attempt to connect to 127.0.0.1:59999 and fail with 502,
        // but load balancer MUST have been invoked first to select the server.
        assertThrows(HttpException.class, () -> handler.forward(request));

        assertTrue(balancerCalled.get(), "Load balancer should have been invoked to balance the connection");
        assertNotNull(capturedContext.get(), "SelectionContext should not be null");
        assertEquals("192.168.1.100", capturedContext.get().clientIp(), "Client IP should be passed in context");
    }

    @Test
    void shouldThrowBadGatewayWhenNoHealthyServersInCluster() throws Exception {
        Cluster emptyCluster = new Cluster("empty-cluster", Collections.emptySet());

        Router router = new Router();
        router.add("/empty", emptyCluster, HttpMethod.GET);

        Client client = new Client("192.168.1.100", 54321, 8080);
        UpstreamHandler handler = new UpstreamHandler(router.build(), client, BalancingStrategy.ROUND_ROBIN);

        HttpRequest request = HttpRequest.builder()
                .path("/empty")
                .method(HttpMethod.GET)
                .version(HttpVersion.HTTP_1_1)
                .headers(new HashMap<>())
                .build();

        HttpException exception = assertThrows(HttpException.class, () -> handler.forward(request));
        assertEquals(502, exception.getStatus());
        assertTrue(exception.getMessage().contains("No healthy servers"));
    }
}
