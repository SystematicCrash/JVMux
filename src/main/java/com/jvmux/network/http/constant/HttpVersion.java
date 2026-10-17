package com.jvmux.network.http.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum HttpVersion {
    HTTP_1_0("HTTP/1.0"),
    HTTP_1_1("HTTP/1.1"),
    HTTP_2("HTTP/2"),
    HTTP_3("HTTP/3");

    private final String value;

    // Precomputed O(1) reverse-lookup table populated once at class-load
    private static final Map<String, HttpVersion> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    HttpVersion::value,
                    Function.identity()
            ));

    HttpVersion(String value) {
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
     * Safe lookup returning Optional.
     * Zero exception overhead on unknown/malformed versions.
     */
    public static Optional<HttpVersion> find(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(LOOKUP.get(raw));
    }

    /**
     * Fast O(1) validation check without allocations or exceptions.
     */
    public static boolean isValid(String raw) {
        return raw != null && LOOKUP.containsKey(raw);
    }

    /**
     * Strict parser for callers expecting an explicit value or a descriptive error.
     */
    public static HttpVersion from(String raw) {
        return find(raw).orElseThrow(() ->
                new IllegalArgumentException("Unsupported or invalid HTTP version: " + raw));
    }
}