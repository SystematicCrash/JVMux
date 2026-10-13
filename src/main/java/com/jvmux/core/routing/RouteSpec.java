package com.jvmux.core.routing;

import com.jvmux.core.http.HttpMethod;
import lombok.Builder;

import java.util.Set;

@Builder
public record RouteSpec(
        String path,
        Set<HttpMethod> methods,
        Handler handler
) {}
