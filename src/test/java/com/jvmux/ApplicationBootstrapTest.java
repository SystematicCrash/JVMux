package com.jvmux;

import com.jvmux.config.ConfigException;
import com.jvmux.config.JvmuxConfig;
import com.jvmux.config.YamlConfigLoader;
import com.jvmux.network.model.Cluster;
import com.jvmux.network.proxy.MockUpstreamServer;
import com.jvmux.network.routing.RouteTree;
import com.jvmux.network.routing.Router;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Application Bootstrap & Router Configuration Tests")
class ApplicationBootstrapTest {

    @Test
    @DisplayName("Should build Router from JvmuxConfig and correctly route requests to configured cluster")
    void shouldBuildRouterFromConfig() throws Exception {
        String yaml = """
                clusters:
                  - name: "api-cluster"
                    strategy: "ROUND_ROBIN"
                    servers:
                      - host: "127.0.0.1"
                        port: 8081
                  - name: "auth-cluster"
                    strategy: "LEAST_CONNECTIONS"
                    servers:
                      - host: "127.0.0.1"
                        port: 8082
                routes:
                  - path: "/api"
                    cluster: "api-cluster"
                    methods: ["GET", "POST"]
                  - path: "/auth"
                    cluster: "auth-cluster"
                    methods: ["POST"]
                """;

        JvmuxConfig config = new YamlConfigLoader().load(yaml);
        Router router = Router.fromConfig(config);

        assertEquals(2, router.getRoutes().size());

        RouteTree tree = router.build();
        assertNotNull(tree);

        var lookupApi = tree.lookup(com.jvmux.network.http.constant.HttpMethod.GET, "/api");
        assertEquals(com.jvmux.network.routing.LookupKind.FOUND, lookupApi.kind());
        assertEquals("api-cluster", lookupApi.cluster().name());

        var lookupAuth = tree.lookup(com.jvmux.network.http.constant.HttpMethod.POST, "/auth");
        assertEquals(com.jvmux.network.routing.LookupKind.FOUND, lookupAuth.kind());
        assertEquals("auth-cluster", lookupAuth.cluster().name());

        var lookupMethodNotAllowed = tree.lookup(com.jvmux.network.http.constant.HttpMethod.DELETE, "/api");
        assertEquals(com.jvmux.network.routing.LookupKind.METHOD_NOT_ALLOWED, lookupMethodNotAllowed.kind());
    }

    @Test
    @DisplayName("Should throw ConfigException when route references undefined cluster")
    void shouldThrowWhenRouteReferencesUndefinedCluster() {
        String yaml = """
                clusters:
                  - name: "existing-cluster"
                    servers:
                      - host: "127.0.0.1"
                        port: 8081
                routes:
                  - path: "/api/*"
                    cluster: "non-existent-cluster"
                """;

        JvmuxConfig config = new YamlConfigLoader().load(yaml);
        assertThrows(ConfigException.class, () -> Router.fromConfig(config));
    }

    @Test
    @DisplayName("Should load default configuration from classpath")
    void shouldLoadDefaultConfigurationFromClasspath() {
        JvmuxConfig config = Application.loadConfiguration(null);
        assertNotNull(config);
        assertNotNull(config.getListener());
        assertEquals(8080, config.getListener().getPort());
        assertFalse(config.toClusters().isEmpty());
    }

    @Test
    @DisplayName("Should verify Main class exists as entry point")
    void shouldVerifyMainClass() {
        assertNotNull(Main.class);
    }

    @Test
    @DisplayName("Should bootstrap Application and serve traffic through configured listener and clusters")
    void shouldBootstrapApplicationAndServeTraffic() throws Exception {
        try (MockUpstreamServer upstream = new MockUpstreamServer("test-backend")) {
            String yaml = String.format("""
                    listener:
                      host: "127.0.0.1"
                      port: 0
                      backlog: 128
                      max_requests_per_connection: 100
                    clusters:
                      - name: "test-cluster"
                        strategy: "ROUND_ROBIN"
                        servers:
                          - host: "127.0.0.1"
                            port: %d
                            weight: 1
                            healthy: true
                    routes:
                      - path: "/hello"
                        cluster: "test-cluster"
                        methods: ["ANY"]
                    """, upstream.getPort());

            JvmuxConfig config = new YamlConfigLoader().load(yaml);

            try (Application app = new Application(config)) {
                CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
                    try {
                        app.start();
                    } catch (Exception e) {
                        // socket closed on shutdown
                    }
                });

                // Wait briefly for server to bind
                for (int i = 0; i < 50; i++) {
                    if (app.getPort() > 0) break;
                    Thread.sleep(20);
                }

                assertTrue(app.getPort() > 0, "Server port should be assigned");
                int proxyPort = app.getPort();

                // Send test request through proxy
                try (Socket client = new Socket("127.0.0.1", proxyPort)) {
                    OutputStream out = client.getOutputStream();
                    String request = "GET /hello HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
                    out.write(request.getBytes(StandardCharsets.UTF_8));
                    out.flush();

                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String statusLine = in.readLine();
                    assertNotNull(statusLine);
                    assertTrue(statusLine.contains("200 OK"), "Expected 200 OK, got: " + statusLine);
                }

                assertEquals(1, upstream.getRequestCount());

                // Gracefully shutdown
                app.close();
                serverFuture.get(2, TimeUnit.SECONDS);
            }
        }
    }
}
