package com.jvmux.core.routing;

import com.jvmux.core.http.constant.HttpMethod;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public class Router {
    private final List<RouteSpec> routes = new ArrayList<>();

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
