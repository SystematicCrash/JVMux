package com.jvmux.network.routing;

import com.jvmux.network.http.constant.HttpMethod;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public class Router {
    private final List<RouteSpec> routes = new ArrayList<>();

    public void add(String path, Handler handler, HttpMethod... methods) {
        Set<HttpMethod> methodSet;
        
        if (methods.length == 0) methodSet = Set.of(HttpMethod.ANY);
        else methodSet = Set.of(methods);
        
        this.routes.add(
                RouteSpec.builder()
                        .methods(methodSet)
                        .path(path)
                        .handler(handler)
                        .build()
        );
    }

    public RouteTree build() throws Exception {
        return RouteTree.from(this.routes);
    }
}
