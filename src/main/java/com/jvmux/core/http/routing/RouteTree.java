package com.jvmux.core.http.routing;

import com.jvmux.core.http.HttpMethod;
import com.jvmux.core.http.routing.model.lookup.LookupResult;
import com.jvmux.core.http.routing.model.route.RouteSpec;

import java.util.HashMap;
import java.util.List;

public class RouteTree {
    private RouteNode root;

    public static RouteTree from(List<RouteSpec> routes) throws Exception {
        return new RouteTree().buildTree(routes);
    }

    public LookupResult lookup(HttpMethod method, String path) {
        var segments = path.split("/");
        return this.root.lookup(method, segments, 0, new HashMap<>());
    }

    private String normalizePath(String path) throws Exception {
        if (path == null) {
            throw new Exception("");
        }
        if (path.equals("/") || path.isEmpty()) {
            return path;
        }
        if (!path.startsWith("/")) {
            throw new Exception("Route path must start with '/': " + path);
        }
        return path.replaceAll("/+$", "");
    }

    private RouteTree buildTree(List<RouteSpec> routes) throws Exception {
        this.root = new RouteNode();

        for (var route : routes) {
            var path = this.normalizePath(route.path());
            var segments = path.split("/");
            var node = root;

            for (String segment : segments) {
                node = node.insert(segment);
            }

            for (HttpMethod method : route.methods()) {
                if (method == HttpMethod.ANY) {
                    node.handlers.clear();
                    node.handlers.put(method, route.handler());
                    break;
                }
                node.handlers.put(method, route.handler());
            }
        }
        return this;
    }
}
