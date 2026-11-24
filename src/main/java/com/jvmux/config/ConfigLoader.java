package com.jvmux.config;

import com.jvmux.network.model.Cluster;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface ConfigLoader {

    JvmuxConfig load(InputStream inputStream);

    JvmuxConfig load(Path path);

    JvmuxConfig load(String yamlContent);

    List<Cluster> loadClusters(InputStream inputStream);

    List<Cluster> loadClusters(Path path);

    List<Cluster> loadClusters(String yamlContent);

    Map<String, Cluster> loadClusterMap(InputStream inputStream);

    Map<String, Cluster> loadClusterMap(Path path);

    Map<String, Cluster> loadClusterMap(String yamlContent);
}
