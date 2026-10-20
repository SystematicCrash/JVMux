package com.jvmux.network.routing;

import com.jvmux.network.http.constant.HttpMethod;
import lombok.Getter;

import java.util.*;

import static com.jvmux.network.http.constant.HttpMethod.*;

@Getter
public class Router {

    private final List<RouteSpec> routes = new ArrayList<>();

    public Router() {
        this.registerDefaultRoutes();
    }

    public void add(String path, Handler handler, HttpMethod... methods) {
        var set = EnumSet.noneOf(HttpMethod.class);

        if (methods.length == 0) set = EnumSet.of(HttpMethod.ANY);
        else Collections.addAll(set, methods);

        this.routes.add(
                RouteSpec.builder()
                        .methods(set)
                        .path(path)
                        .handler(handler)
                        .build()
        );
    }

    public RouteTree build() throws Exception {
        return RouteTree.from(this.routes);
    }

    private void registerDefaultRoutes() {
        this.add("/users/profile", new Handler(), GET, DELETE, POST);
    }
}
