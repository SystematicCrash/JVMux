package com.jvmux.network.routing;

import com.jvmux.config.ConfigException;
import com.jvmux.config.JvmuxConfig;
import com.jvmux.config.RouteConfig;
import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.model.Cluster;
import lombok.Getter;

import java.util.*;

import static com.jvmux.network.http.constant.HttpMethod.*;

@Getter
public class Router {

    private final List<RouteSpec> routes = new ArrayList<>();

    public Router() {
        this(true);
    }

    public Router(boolean includeDefaultRoutes) {
        if (includeDefaultRoutes) {
            this.registerDefaultRoutes();
        }
    }

    public void add(String path, Cluster handler, HttpMethod... methods) {
        var set = EnumSet.noneOf(HttpMethod.class);

        if (methods.length == 0) set = EnumSet.of(HttpMethod.ANY);
        else Collections.addAll(set, methods);

        this.routes.add(
                RouteSpec.builder()
                        .methods(set)
                        .path(path)
                        .cluster(handler)
                        .build()
        );
    }

    public void add(String path, Cluster handler, Set<HttpMethod> methods) {
        var set = (methods == null || methods.isEmpty())
                ? EnumSet.of(HttpMethod.ANY)
                : EnumSet.copyOf(methods);

        this.routes.add(
                RouteSpec.builder()
                        .methods(set)
                        .path(path)
                        .cluster(handler)
                        .build()
        );
    }

    public static Router fromConfig(JvmuxConfig config) {
        Objects.requireNonNull(config, "JvmuxConfig cannot be null");
        Router router = new Router(false);
        Map<String, Cluster> clusterMap = config.toClusterMap();

        if (config.getRoutes() != null) {
            for (RouteConfig routeConfig : config.getRoutes()) {
                if (routeConfig == null) continue;
                String clusterName = routeConfig.getCluster();
                Cluster cluster = clusterMap.get(clusterName);
                if (cluster == null) {
                    throw new ConfigException("Route path '" + routeConfig.getPath() +
                            "' references unknown cluster: '" + clusterName + "'");
                }
                router.add(routeConfig.getPath(), cluster, routeConfig.resolveMethods());
            }
        }

        return router;
    }

    public RouteTree build() throws Exception {
        return RouteTree.from(this.routes);
    }

    private void registerDefaultRoutes() {
        this.add("/", new Cluster("Dummy", Set.of()), GET, DELETE, POST);
    }
}
