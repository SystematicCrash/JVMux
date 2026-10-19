package com.jvmux.net.routing;

import com.jvmux.net.http.constant.HttpMethod;
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