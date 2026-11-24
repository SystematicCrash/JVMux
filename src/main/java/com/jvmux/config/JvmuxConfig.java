package com.jvmux.config;

import com.jvmux.network.model.Cluster;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JvmuxConfig {

    @Builder.Default
    private ListenerConfig listener = new ListenerConfig();

    @Builder.Default
    private List<ClusterConfig> clusters = new ArrayList<>();

    @Builder.Default
    private List<RouteConfig> routes = new ArrayList<>();

    public List<Cluster> toClusters() {
        List<Cluster> result = new ArrayList<>();
        if (clusters != null) {
            for (ClusterConfig clusterConfig : clusters) {
                if (clusterConfig != null) {
                    result.add(clusterConfig.toCluster());
                }
            }
        }
        return result;
    }

    public Map<String, Cluster> toClusterMap() {
        Map<String, Cluster> map = new LinkedHashMap<>();
        for (Cluster cluster : toClusters()) {
            if (map.containsKey(cluster.name())) {
                throw new ConfigException("Duplicate cluster name found: " + cluster.name());
            }
            map.put(cluster.name(), cluster);
        }
        return map;
    }
}
