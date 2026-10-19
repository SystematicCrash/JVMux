package com.jvmux.network.routing;

import com.jvmux.network.http.constant.HttpMethod;
import lombok.Builder;

import java.util.Set;

@Builder
public record RouteSpec(
        String path,
        Set<HttpMethod> methods,
        Handler handler
) {}
