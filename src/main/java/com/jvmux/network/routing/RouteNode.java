package com.jvmux.network.routing;

import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.upstream.Cluster;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Optional;
import java.util.Map;

public class RouteNode {
    private final Map<String, RouteNode> children = new HashMap<>();
    public final EnumMap<HttpMethod, Cluster> clusters = new EnumMap<>(HttpMethod.class);

    public LookupResult lookup(HttpMethod method, String[] segments, int idx, Map<String, String> params) {
        if (idx == segments.length) {
            return this.lookupClusters(method);
        }

        var segment = segments[idx];

        return this.lookupChildren(segment, method, segments, idx + 1, params);
    }

    public RouteNode insert(String segment) {
        return this.children.computeIfAbsent(segment, k -> new RouteNode());
    }

    private LookupResult lookupClusters(HttpMethod method) {
        var clusterOpt = Optional.ofNullable(this.clusters.get(method))
                .or(() -> Optional.ofNullable(this.clusters.get(HttpMethod.ANY)));

        if (clusterOpt.isPresent()) {
            return LookupResult.builder()
                    .kind(LookupKind.FOUND)
                    .cluster(clusterOpt.get())
                    .build();
        }

        if (!this.clusters.isEmpty()) {
            return LookupResult.builder()
                    .kind(LookupKind.METHOD_NOT_ALLOWED)
                    .allowed(this.clusters.keySet())
                    .build();
        }

        return LookupResult.builder()
                .kind(LookupKind.NOT_FOUND)
                .build();
    }

    private LookupResult lookupChildren(
            String segment,
            HttpMethod method,
            String[] segments,
            int nextIdx,
            Map<String, String> params
    ) {
        var child = this.children.get(segment);
        
        if (child == null) {
            return this.lookupClusters(method);
        }
        return child.lookup(method, segments, nextIdx, params);
    }
}