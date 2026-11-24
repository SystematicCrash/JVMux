package com.jvmux.config;

import com.jvmux.network.http.constant.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteConfig {

    private String path;
    private String cluster;

    @Builder.Default
    private List<String> methods = new ArrayList<>();

    public EnumSet<HttpMethod> resolveMethods() {
        if (methods == null || methods.isEmpty()) {
            return EnumSet.of(HttpMethod.ANY);
        }

        EnumSet<HttpMethod> methodSet = EnumSet.noneOf(HttpMethod.class);
        for (String m : methods) {
            if (m == null || m.isBlank()) continue;
            String normalized = m.trim().toUpperCase();
            if ("*".equals(normalized) || "ANY".equals(normalized)) {
                return EnumSet.of(HttpMethod.ANY);
            }
            try {
                methodSet.add(HttpMethod.valueOf(normalized));
            } catch (IllegalArgumentException e) {
                throw new ConfigException("Unknown HTTP method: '" + m + "' in route path '" + path + "'", e);
            }
        }

        return methodSet.isEmpty() ? EnumSet.of(HttpMethod.ANY) : methodSet;
    }
}
