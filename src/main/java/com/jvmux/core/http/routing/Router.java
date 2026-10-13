package com.jvmux.core.http.routing;

import com.jvmux.core.http.HttpMethod;
import com.jvmux.core.http.routing.model.route.Handler;
import com.jvmux.core.http.routing.model.route.RouteSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Router {
    private final List<RouteSpec> routes = new ArrayList<>();

    public List<RouteSpec> getRoutes() {
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

    public RouteTree build() throws Exception {
        return RouteTree.from(this.routes);
    }
}
