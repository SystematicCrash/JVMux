package com.jvmux.network.routing;

import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.model.Cluster;
import lombok.Builder;

import java.util.EnumSet;

@Builder
public record RouteSpec(
        String path,
        EnumSet<HttpMethod> methods,
        Cluster cluster
) {}
