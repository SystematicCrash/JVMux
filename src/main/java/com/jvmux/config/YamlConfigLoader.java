package com.jvmux.config;

import com.jvmux.network.model.Cluster;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class YamlConfigLoader implements ConfigLoader {

    private final Yaml yaml;

    public YamlConfigLoader() {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);
        loaderOptions.setMaxAliasesForCollections(50);
        this.yaml = new Yaml(new SafeConstructor(loaderOptions));
    }

    @Override
    public JvmuxConfig load(InputStream inputStream) {
        if (inputStream == null) {
            throw new ConfigException("Input stream cannot be null");
        }

        try {
            Object parsed = yaml.load(inputStream);
            return parseJvmuxConfig(parsed);
        } catch (ConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigException("Failed to parse YAML configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public JvmuxConfig load(Path path) {
        if (path == null) {
            throw new ConfigException("Path cannot be null");
        }
        if (!Files.exists(path)) {
            throw new ConfigException("Configuration file does not exist: " + path);
        }

        try (InputStream in = Files.newInputStream(path)) {
            return load(in);
        } catch (IOException e) {
            throw new ConfigException("Failed to read configuration file: " + path, e);
        }
    }

    @Override
    public JvmuxConfig load(String yamlContent) {
        if (yamlContent == null || yamlContent.isBlank()) {
            throw new ConfigException("YAML content cannot be null or blank");
        }
        return load(new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8)));
    }

    public JvmuxConfig loadFromClasspath(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new ConfigException("Classpath resource cannot be null or blank");
        }

        String path = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) {
                throw new ConfigException("Resource not found on classpath: " + resourcePath);
            }
            return load(in);
        } catch (IOException e) {
            throw new ConfigException("Failed to read classpath resource: " + resourcePath, e);
        }
    }

    @Override
    public List<Cluster> loadClusters(InputStream inputStream) {
        return load(inputStream).toClusters();
    }

    @Override
    public List<Cluster> loadClusters(Path path) {
        return load(path).toClusters();
    }

    @Override
    public List<Cluster> loadClusters(String yamlContent) {
        return load(yamlContent).toClusters();
    }

    @Override
    public Map<String, Cluster> loadClusterMap(InputStream inputStream) {
        return load(inputStream).toClusterMap();
    }

    @Override
    public Map<String, Cluster> loadClusterMap(Path path) {
        return load(path).toClusterMap();
    }

    @Override
    public Map<String, Cluster> loadClusterMap(String yamlContent) {
        return load(yamlContent).toClusterMap();
    }

    private JvmuxConfig parseJvmuxConfig(Object root) {
        if (root == null) {
            return new JvmuxConfig();
        }

        if (root instanceof List<?> list) {
            // User provided a top-level list of clusters
            List<ClusterConfig> clusterConfigs = parseClusterList(list);
            return JvmuxConfig.builder().clusters(clusterConfigs).build();
        }

        if (!(root instanceof Map<?, ?> rootMap)) {
            throw new ConfigException("YAML root must be a mapping or a list of clusters, got: " + root.getClass().getSimpleName());
        }

        JvmuxConfig config = new JvmuxConfig();

        // 1. Parse listener if present
        Object listenerObj = rootMap.get("listener");
        if (listenerObj instanceof Map<?, ?> listenerMap) {
            config.setListener(parseListener(listenerMap));
        }

        // 2. Parse clusters
        Object clustersObj = rootMap.get("clusters");
        if (clustersObj instanceof List<?> clusterList) {
            config.setClusters(parseClusterList(clusterList));
        }

        // 3. Parse routes if present
        Object routesObj = rootMap.get("routes");
        if (routesObj instanceof List<?> routeList) {
            config.setRoutes(parseRouteList(routeList));
        }

        return config;
    }

    private ListenerConfig parseListener(Map<?, ?> map) {
        ListenerConfig.ListenerConfigBuilder builder = ListenerConfig.builder();

        if (map.containsKey("host")) {
            builder.host(String.valueOf(map.get("host")));
        }
        if (map.containsKey("port")) {
            builder.port(parseInt(map.get("port"), "listener.port"));
        }
        if (map.containsKey("backlog")) {
            builder.backlog(parseInt(map.get("backlog"), "listener.backlog"));
        }
        if (map.containsKey("max_requests_per_connection") || map.containsKey("maxRequestsPerConnection")) {
            Object val = map.containsKey("max_requests_per_connection")
                    ? map.get("max_requests_per_connection")
                    : map.get("maxRequestsPerConnection");
            builder.maxRequestsPerConnection(parseInt(val, "listener.max_requests_per_connection"));
        }

        return builder.build();
    }

    private List<ClusterConfig> parseClusterList(List<?> list) {
        List<ClusterConfig> clusters = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> clusterMap) {
                clusters.add(parseCluster(clusterMap));
            } else if (item != null) {
                throw new ConfigException("Expected cluster mapping, got: " + item.getClass().getSimpleName());
            }
        }
        return clusters;
    }

    private ClusterConfig parseCluster(Map<?, ?> map) {
        Object nameObj = map.get("name");
        if (nameObj == null || String.valueOf(nameObj).isBlank()) {
            throw new ConfigException("Cluster name is required and cannot be empty");
        }

        String name = String.valueOf(nameObj).trim();
        String strategy = map.containsKey("strategy") ? String.valueOf(map.get("strategy")) : null;

        List<ServerConfig> servers = new ArrayList<>();
        Object serversObj = map.get("servers");
        if (serversObj instanceof List<?> serverList) {
            for (Object sObj : serverList) {
                if (sObj instanceof Map<?, ?> serverMap) {
                    servers.add(parseServer(serverMap));
                } else if (sObj != null) {
                    throw new ConfigException("Expected server mapping in cluster '" + name + "', got: " + sObj.getClass().getSimpleName());
                }
            }
        }

        return ClusterConfig.builder()
                .name(name)
                .strategy(strategy)
                .servers(servers)
                .build();
    }

    private ServerConfig parseServer(Map<?, ?> map) {
        ServerConfig.ServerConfigBuilder builder = ServerConfig.builder();

        if (map.containsKey("host")) {
            builder.host(String.valueOf(map.get("host")));
        }
        if (map.containsKey("port")) {
            builder.port(parseInt(map.get("port"), "server.port"));
        }
        if (map.containsKey("weight")) {
            builder.weight(parseInt(map.get("weight"), "server.weight"));
        }
        if (map.containsKey("healthy")) {
            builder.healthy(Boolean.parseBoolean(String.valueOf(map.get("healthy"))));
        }

        return builder.build();
    }

    private List<RouteConfig> parseRouteList(List<?> list) {
        List<RouteConfig> routes = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> routeMap) {
                routes.add(parseRoute(routeMap));
            }
        }
        return routes;
    }

    private RouteConfig parseRoute(Map<?, ?> map) {
        String path = map.containsKey("path") ? String.valueOf(map.get("path")) : "/*";
        String cluster = map.containsKey("cluster") ? String.valueOf(map.get("cluster")) : null;

        List<String> methods = new ArrayList<>();
        Object methodsObj = map.get("methods");
        if (methodsObj instanceof List<?> mList) {
            for (Object m : mList) {
                if (m != null) methods.add(String.valueOf(m));
            }
        } else if (methodsObj != null) {
            methods.add(String.valueOf(methodsObj));
        }

        return RouteConfig.builder()
                .path(path)
                .cluster(cluster)
                .methods(methods)
                .build();
    }

    private int parseInt(Object value, String fieldName) {
        if (value instanceof Number num) {
            return num.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new ConfigException("Invalid integer value for field '" + fieldName + "': " + value, e);
        }
    }
}
