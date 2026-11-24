package com.jvmux.config;

import com.jvmux.network.balancer.BalancingStrategy;
import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("YamlConfigLoader Tests")
class YamlConfigLoaderTest {

    private YamlConfigLoader loader;

    @BeforeEach
    void setUp() {
        loader = new YamlConfigLoader();
    }

    @Test
    @DisplayName("Should parse full YAML configuration into JvmuxConfig")
    void shouldParseFullYamlConfiguration() {
        String yaml = """
                listener:
                  host: "127.0.0.1"
                  port: 9090
                  backlog: 512
                  max_requests_per_connection: 500
                clusters:
                  - name: "api-cluster"
                    strategy: "LEAST_CONNECTIONS"
                    servers:
                      - host: "192.168.1.10"
                        port: 8081
                        weight: 4
                        healthy: true
                      - host: "192.168.1.20"
                        port: 8082
                        weight: 2
                        healthy: false
                routes:
                  - path: "/api/*"
                    cluster: "api-cluster"
                    methods: ["GET", "POST"]
                """;

        JvmuxConfig config = loader.load(yaml);

        assertNotNull(config);
        assertEquals("127.0.0.1", config.getListener().getHost());
        assertEquals(9090, config.getListener().getPort());
        assertEquals(512, config.getListener().getBacklog());
        assertEquals(500, config.getListener().getMaxRequestsPerConnection());

        assertEquals(1, config.getClusters().size());
        ClusterConfig clusterConfig = config.getClusters().getFirst();
        assertEquals("api-cluster", clusterConfig.getName());
        assertEquals("LEAST_CONNECTIONS", clusterConfig.getStrategy());
        assertEquals(BalancingStrategy.LEAST_CONNECTIONS, clusterConfig.resolveStrategy());

        assertEquals(2, clusterConfig.getServers().size());
        ServerConfig s1 = clusterConfig.getServers().get(0);
        assertEquals("192.168.1.10", s1.getHost());
        assertEquals(8081, s1.getPort());
        assertEquals(4, s1.getWeight());
        assertTrue(s1.isHealthy());

        ServerConfig s2 = clusterConfig.getServers().get(1);
        assertEquals("192.168.1.20", s2.getHost());
        assertEquals(8082, s2.getPort());
        assertEquals(2, s2.getWeight());
        assertFalse(s2.isHealthy());

        assertEquals(1, config.getRoutes().size());
        RouteConfig route = config.getRoutes().getFirst();
        assertEquals("/api/*", route.getPath());
        assertEquals("api-cluster", route.getCluster());
        assertEquals(List.of("GET", "POST"), route.getMethods());
    }

    @Test
    @DisplayName("Should convert ClusterConfig into domain Cluster and Server objects")
    void shouldConvertClusterConfigToDomainCluster() {
        String yaml = """
                clusters:
                  - name: "web-cluster"
                    strategy: "WEIGHTED_ROUND_ROBIN"
                    servers:
                      - host: "10.0.0.1"
                        port: 8080
                        weight: 5
                        healthy: true
                      - host: "10.0.0.2"
                        port: 8080
                        weight: 3
                        healthy: true
                """;

        List<Cluster> clusters = loader.loadClusters(yaml);

        assertEquals(1, clusters.size());
        Cluster cluster = clusters.getFirst();
        assertEquals("web-cluster", cluster.name());
        assertEquals(BalancingStrategy.WEIGHTED_ROUND_ROBIN, cluster.strategy());
        assertEquals(2, cluster.servers().size());

        for (Server server : cluster.servers()) {
            assertTrue(server.isHealthy());
            assertTrue(server.getPort() == 8080);
            assertTrue(server.getHost().equals("10.0.0.1") || server.getHost().equals("10.0.0.2"));
            assertTrue(server.getWeight() == 5 || server.getWeight() == 3);
        }
    }

    @Test
    @DisplayName("Should default cluster balancing strategy to ROUND_ROBIN when strategy is omitted or empty")
    void shouldDefaultStrategyToRoundRobinWhenOmitted() {
        String yaml = """
                clusters:
                  - name: "default-cluster"
                    servers:
                      - host: "127.0.0.1"
                        port: 8080
                        weight: 1
                        healthy: true
                  - name: "empty-strategy-cluster"
                    strategy: ""
                    servers:
                      - host: "127.0.0.1"
                        port: 8081
                """;

        List<Cluster> clusters = loader.loadClusters(yaml);

        assertEquals(2, clusters.size());
        assertEquals(BalancingStrategy.ROUND_ROBIN, clusters.get(0).strategy());
        assertEquals(BalancingStrategy.ROUND_ROBIN, clusters.get(1).strategy());
    }

    @Test
    @DisplayName("Should parse various balancing strategy name formats (case-insensitive, kebab-case, snake_case)")
    void shouldParseVariousBalancingStrategyFormats() {
        String yaml = """
                clusters:
                  - name: "c1"
                    strategy: "round_robin"
                  - name: "c2"
                    strategy: "weighted-round-robin"
                  - name: "c3"
                    strategy: "least_connections"
                  - name: "c4"
                    strategy: "weighted-least-connections"
                  - name: "c5"
                    strategy: "power_of_two_choices"
                  - name: "c6"
                    strategy: "consistent-hash"
                  - name: "c7"
                    strategy: "ip_hash"
                  - name: "c8"
                    strategy: "peak-ewma"
                  - name: "c9"
                    strategy: "random"
                  - name: "c10"
                    strategy: "weighted_random"
                """;

        List<Cluster> clusters = loader.loadClusters(yaml);

        assertEquals(10, clusters.size());
        assertEquals(BalancingStrategy.ROUND_ROBIN, clusters.get(0).strategy());
        assertEquals(BalancingStrategy.WEIGHTED_ROUND_ROBIN, clusters.get(1).strategy());
        assertEquals(BalancingStrategy.LEAST_CONNECTIONS, clusters.get(2).strategy());
        assertEquals(BalancingStrategy.WEIGHTED_LEAST_CONNECTIONS, clusters.get(3).strategy());
        assertEquals(BalancingStrategy.POWER_OF_TWO_CHOICES, clusters.get(4).strategy());
        assertEquals(BalancingStrategy.CONSISTENT_HASH, clusters.get(5).strategy());
        assertEquals(BalancingStrategy.IP_HASH, clusters.get(6).strategy());
        assertEquals(BalancingStrategy.PEAK_EWMA, clusters.get(7).strategy());
        assertEquals(BalancingStrategy.RANDOM, clusters.get(8).strategy());
        assertEquals(BalancingStrategy.WEIGHTED_RANDOM, clusters.get(9).strategy());
    }

    @Test
    @DisplayName("Should parse top-level list of clusters directly")
    void shouldParseTopLevelListOfClusters() {
        String yaml = """
                - name: "service-a"
                  strategy: "ROUND_ROBIN"
                  servers:
                    - host: "127.0.0.1"
                      port: 8081
                - name: "service-b"
                  strategy: "LEAST_CONNECTIONS"
                  servers:
                    - host: "127.0.0.1"
                      port: 8082
                """;

        List<Cluster> clusters = loader.loadClusters(yaml);

        assertEquals(2, clusters.size());
        assertEquals("service-a", clusters.get(0).name());
        assertEquals(BalancingStrategy.ROUND_ROBIN, clusters.get(0).strategy());
        assertEquals("service-b", clusters.get(1).name());
        assertEquals(BalancingStrategy.LEAST_CONNECTIONS, clusters.get(1).strategy());
    }

    @Test
    @DisplayName("Should convert to cluster map correctly")
    void shouldConvertToClusterMap() {
        String yaml = """
                clusters:
                  - name: "cluster-1"
                    servers:
                      - host: "127.0.0.1"
                        port: 8001
                  - name: "cluster-2"
                    servers:
                      - host: "127.0.0.1"
                        port: 8002
                """;

        Map<String, Cluster> map = loader.loadClusterMap(yaml);

        assertEquals(2, map.size());
        assertTrue(map.containsKey("cluster-1"));
        assertTrue(map.containsKey("cluster-2"));
        assertEquals("cluster-1", map.get("cluster-1").name());
        assertEquals("cluster-2", map.get("cluster-2").name());
    }

    @Test
    @DisplayName("Should throw ConfigException on duplicate cluster names")
    void shouldThrowOnDuplicateClusterNames() {
        String yaml = """
                clusters:
                  - name: "dup-cluster"
                    servers:
                      - host: "127.0.0.1"
                        port: 8001
                  - name: "dup-cluster"
                    servers:
                      - host: "127.0.0.1"
                        port: 8002
                """;

        JvmuxConfig config = loader.load(yaml);
        assertThrows(ConfigException.class, config::toClusterMap);
    }

    @Test
    @DisplayName("Should throw ConfigException on unknown balancing strategy")
    void shouldThrowOnUnknownBalancingStrategy() {
        String yaml = """
                clusters:
                  - name: "bad-strategy-cluster"
                    strategy: "INVALID_STRATEGY"
                    servers:
                      - host: "127.0.0.1"
                        port: 8001
                """;

        assertThrows(ConfigException.class, () -> loader.loadClusters(yaml));
    }

    @Test
    @DisplayName("Should throw ConfigException when cluster name is missing")
    void shouldThrowWhenClusterNameIsMissing() {
        String yaml = """
                clusters:
                  - strategy: "ROUND_ROBIN"
                    servers:
                      - host: "127.0.0.1"
                        port: 8001
                """;

        assertThrows(ConfigException.class, () -> loader.load(yaml));
    }

    @Test
    @DisplayName("Should throw ConfigException when server port is invalid")
    void shouldThrowWhenServerPortIsInvalid() {
        String yamlNegative = """
                clusters:
                  - name: "test-cluster"
                    servers:
                      - host: "127.0.0.1"
                        port: -1
                """;
        assertThrows(ConfigException.class, () -> loader.loadClusters(yamlNegative));

        String yamlTooHigh = """
                clusters:
                  - name: "test-cluster"
                    servers:
                      - host: "127.0.0.1"
                        port: 70000
                """;
        assertThrows(ConfigException.class, () -> loader.loadClusters(yamlTooHigh));
    }

    @Test
    @DisplayName("Should throw ConfigException when server weight is invalid")
    void shouldThrowWhenServerWeightIsInvalid() {
        String yamlZeroWeight = """
                clusters:
                  - name: "test-cluster"
                    servers:
                      - host: "127.0.0.1"
                        port: 8080
                        weight: 0
                """;
        assertThrows(ConfigException.class, () -> loader.loadClusters(yamlZeroWeight));

        String yamlExcessWeight = """
                clusters:
                  - name: "test-cluster"
                    servers:
                      - host: "127.0.0.1"
                        port: 8080
                        weight: 15
                """;
        assertThrows(ConfigException.class, () -> loader.loadClusters(yamlExcessWeight));
    }

    @Test
    @DisplayName("Should read from file Path and InputStream")
    void shouldReadFromFilePathAndInputStream(@TempDir Path tempDir) throws Exception {
        String content = """
                clusters:
                  - name: "file-cluster"
                    strategy: "PEAK_EWMA"
                    servers:
                      - host: "127.0.0.1"
                        port: 9999
                """;

        Path file = tempDir.resolve("config.yaml");
        Files.writeString(file, content);

        JvmuxConfig fromPath = loader.load(file);
        assertEquals(1, fromPath.getClusters().size());
        assertEquals("file-cluster", fromPath.getClusters().getFirst().getName());
        assertEquals(BalancingStrategy.PEAK_EWMA, fromPath.getClusters().getFirst().resolveStrategy());

        JvmuxConfig fromStream = loader.load(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        assertEquals(1, fromStream.getClusters().size());
        assertEquals("file-cluster", fromStream.getClusters().getFirst().getName());
    }

    @Test
    @DisplayName("Should successfully load bundled sample configuration from classpath")
    void shouldLoadBundledSampleConfigurationFromClasspath() {
        JvmuxConfig config = loader.loadFromClasspath("jvmux.yaml");

        assertNotNull(config);
        assertNotNull(config.getListener());
        assertEquals(8080, config.getListener().getPort());
        assertEquals("0.0.0.0", config.getListener().getHost());

        List<Cluster> clusters = config.toClusters();
        assertFalse(clusters.isEmpty());
        assertTrue(clusters.size() >= 8);

        Map<String, Cluster> clusterMap = config.toClusterMap();
        assertTrue(clusterMap.containsKey("default-api-cluster"));
        assertEquals(BalancingStrategy.ROUND_ROBIN, clusterMap.get("default-api-cluster").strategy());

        assertTrue(clusterMap.containsKey("payments-cluster"));
        assertEquals(BalancingStrategy.WEIGHTED_ROUND_ROBIN, clusterMap.get("payments-cluster").strategy());

        assertTrue(clusterMap.containsKey("compute-cluster"));
        assertEquals(BalancingStrategy.LEAST_CONNECTIONS, clusterMap.get("compute-cluster").strategy());

        assertTrue(clusterMap.containsKey("session-cache-cluster"));
        assertEquals(BalancingStrategy.CONSISTENT_HASH, clusterMap.get("session-cache-cluster").strategy());

        assertTrue(clusterMap.containsKey("auth-cluster"));
        assertEquals(BalancingStrategy.IP_HASH, clusterMap.get("auth-cluster").strategy());

        assertTrue(clusterMap.containsKey("analytics-cluster"));
        assertEquals(BalancingStrategy.PEAK_EWMA, clusterMap.get("analytics-cluster").strategy());

        assertTrue(clusterMap.containsKey("media-cluster"));
        assertEquals(BalancingStrategy.POWER_OF_TWO_CHOICES, clusterMap.get("media-cluster").strategy());
    }
}
