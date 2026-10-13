package com.jvmux.core.http.routing;

import com.jvmux.core.http.HttpMethod;
import com.jvmux.core.http.routing.model.route.Handler;
import com.jvmux.core.http.routing.model.route.RouteSpec;

import java.util.HashSet;
import java.util.Set;

public class Router {
    private final Set<RouteSpec> routes = new HashSet<>();

    public Set<RouteSpec> getRoutes() {
        return this.routes;
    }

    public void add(Set<HttpMethod> methods, String path, Handler handler) {
        this.routes.add(
                RouteSpec.builder()
                        .methods(methods)
                        .path(path)
                        .handler(handler)
                        .build()
        );
    }
}
