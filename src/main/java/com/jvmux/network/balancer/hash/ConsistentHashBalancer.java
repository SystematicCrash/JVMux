package com.jvmux.network.balancer.hash;

import com.jvmux.common.util.XxHasher;
import com.jvmux.network.balancer.AbstractLoadBalancer;
import com.jvmux.network.balancer.SelectionContext;
import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConsistentHashBalancer extends AbstractLoadBalancer {

    private static final int VIRTUAL_NODES = 100;

    private final ConcurrentHashMap<String, Ring> rings = new ConcurrentHashMap<>();

    @Override
    protected Server selectFrom(Cluster cluster, List<Server> healthy, SelectionContext context) {
        if (context == null || context.clientIp() == null) {
            throw new IllegalArgumentException("ClientIp cannot be null");
        }

        return ringFor(cluster, healthy).serverFor(context.clientIp());
    }

    private Ring ringFor(Cluster cluster, List<Server> healthy) {
        List<String> members = memberKey(healthy);
        Ring ring = rings.get(cluster.name());

        if (ring == null || !ring.members().equals(members)) {
            ring = Ring.from(members, healthy);
            rings.put(cluster.name(), ring);
        }

        return ring;
    }

    private static List<String> memberKey(List<Server> servers) {
        return servers.stream()
                .map(Server::getUrl)
                .sorted()
                .toList();
    }

    private record Ring(List<String> members, NavigableMap<Integer, Server> nodes) {

        private static Ring from(List<String> members, List<Server> servers) {
            NavigableMap<Integer, Server> nodes = new TreeMap<>();

            for (Server server : servers) {
                for (int i = 0; i < VIRTUAL_NODES; i++) {
                    nodes.put(XxHasher.hash32(server.getUrl() + "#" + i), server);
                }
            }

            return new Ring(members, nodes);
        }

        private Server serverFor(String key) {
            var entry = nodes.ceilingEntry(XxHasher.hash32(key));

            if (entry == null) {
                entry = nodes.firstEntry();
            }

            return entry.getValue();
        }
    }
}
