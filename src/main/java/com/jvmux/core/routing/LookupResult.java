package com.jvmux.core.routing;

import com.jvmux.core.http.constant.HttpMethod;
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