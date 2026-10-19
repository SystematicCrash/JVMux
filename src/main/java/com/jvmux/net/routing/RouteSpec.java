package com.jvmux.net.routing;

import com.jvmux.net.http.constant.HttpMethod;
import lombok.Builder;

import java.util.Set;

@Builder
public record RouteSpec(
        String path,
        Set<HttpMethod> methods,
        Handler handler
) {}
