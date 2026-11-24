package com.jvmux.config;

import com.jvmux.network.balancer.BalancingStrategy;
import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterConfig {

    private String name;
    private String strategy;

    @Builder.Default
    private List<ServerConfig> servers = new ArrayList<>();

    public BalancingStrategy resolveStrategy() {
        if (strategy == null || strategy.isBlank()) {
            return BalancingStrategy.ROUND_ROBIN;
        }

        String normalized = strategy.trim().toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');

        try {
            return BalancingStrategy.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new ConfigException("Unknown balancing strategy: '" + strategy + "' for cluster '" + name + "'", e);
        }
    }

    public Cluster toCluster() {
        if (name == null || name.isBlank()) {
            throw new ConfigException("Cluster name cannot be null or empty");
        }

        BalancingStrategy balancingStrategy = resolveStrategy();
        Set<Server> domainServers = new LinkedHashSet<>();

        if (servers != null) {
            for (ServerConfig serverConfig : servers) {
                if (serverConfig != null) {
                    domainServers.add(serverConfig.toServer());
                }
            }
        }

        return new Cluster(name, domainServers, balancingStrategy);
    }
}
