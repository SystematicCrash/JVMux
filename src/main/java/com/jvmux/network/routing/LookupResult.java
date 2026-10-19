package com.jvmux.network.routing;

import com.jvmux.network.http.constant.HttpMethod;
import lombok.Builder;
import java.util.Map;
import java.util.Set;

@Builder
public record LookupResult(
    LookupKind kind,
    Handler handler,
    Set<HttpMethod> allowed,
    Map<String, String> params
) {}