package com.jvmux.core.http.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum HttpMethod {
    // Internal wildcard for router matching only
    ANY("ANY"),

    // RFC standard verbs
    GET("GET"),
    PUT("PUT"),
    POST("POST"),
    HEAD("HEAD"),
    PATCH("PATCH"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS"),
    CONNECT("CONNECT"),
    TRACE("TRACE");

    private final String value;

    // Fast O(1) reverse lookup map populated once at startup
    // Explicitly excludes ANY so clients cannot send "ANY" over the wire
    private static final Map<String, HttpMethod> LOOKUP = Arrays.stream(values())
            .filter(method -> method != ANY)
            .collect(Collectors.toUnmodifiableMap(
                    HttpMethod::value,
                    Function.identity()
            ));

    HttpMethod(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Fast O(1) check to see if an incoming raw string is a valid HTTP verb.
     * Zero allocations and zero exception overhead.
     */
    public static boolean isValid(String raw) {
        return raw != null && LOOKUP.containsKey(raw);
    }

    /**
     * Safe lookup returning Optional for null safety and clean pipelines.
     */
    public static Optional<HttpMethod> find(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(LOOKUP.get(raw));
    }

    /**
     * Strict parser for callers expecting an explicit enum value.
     */
    public static HttpMethod from(String raw) {
        return find(raw).orElseThrow(() ->
                new IllegalArgumentException("Unsupported or invalid HTTP method: " + raw));
    }
}