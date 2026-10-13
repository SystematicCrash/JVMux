package com.jvmux.core.http.routing.model.lookup;

import com.jvmux.core.http.HttpMethod;
import com.jvmux.core.http.routing.model.route.Handler;
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