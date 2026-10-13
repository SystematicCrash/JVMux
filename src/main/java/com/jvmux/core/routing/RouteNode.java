package com.jvmux.core.routing;

import com.jvmux.core.http.HttpMethod;

import java.util.HashMap;
import java.util.Optional;
import java.util.Map;

public class RouteNode {
    private final Map<String, RouteNode> children = new HashMap<>();
    public final Map<HttpMethod, Handler> handlers = new HashMap<>();

    public LookupResult lookup(HttpMethod method, String[] segments, int idx, Map<String, String> params) {
        if (idx == segments.length) {
            return this.lookupHandlers(method);
        }

        var segment = segments[idx];

        return this.lookupChildren(segment, method, segments, idx + 1, params)
                .orElseGet(() -> LookupResult.builder()
                        .kind(LookupKind.NOT_FOUND)
                        .build());
    }

    public RouteNode insert(String segment) {
        return this.children.computeIfAbsent(segment, _ -> new RouteNode());
    }

    private LookupResult lookupHandlers(HttpMethod method) {
        var handlerOpt = Optional.ofNullable(this.handlers.get(method))
                .or(() -> Optional.ofNullable(this.handlers.get(HttpMethod.ANY)));

        if (handlerOpt.isPresent()) {
            return LookupResult.builder()
                    .kind(LookupKind.FOUND)
                    .handler(handlerOpt.get())
                    .build();
        }

        if (!this.handlers.isEmpty()) {
            return LookupResult.builder()
                    .kind(LookupKind.METHOD_NOT_ALLOWED)
                    .allowed(this.handlers.keySet())
                    .build();
        }

        return LookupResult.builder()
                .kind(LookupKind.NOT_FOUND)
                .build();
    }

    private Optional<LookupResult> lookupChildren(
            String segment,
            HttpMethod method,
            String[] segments,
            int nextIdx,
            Map<String, String> params
    ) {
        return Optional.ofNullable(this.children.get(segment))
                .map(child -> child.lookup(method, segments, nextIdx, params))
                .filter(res -> res.kind() != LookupKind.NOT_FOUND);
    }
}