package com.jvmux.core.http.constant;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum HttpHeader {

    // === Message Framing & Connection Management ===
    CONNECTION("Connection"),
    CONTENT_LENGTH("Content-Length"),
    TRANSFER_ENCODING("Transfer-Encoding"),
    HOST("Host"),
    TRAILER("Trailer"),
    UPGRADE("Upgrade"),

    // === Representation / Payload Metadata ===
    CONTENT_TYPE("Content-Type"),
    CONTENT_ENCODING("Content-Encoding"),
    CONTENT_LANGUAGE("Content-Language"),
    CONTENT_LOCATION("Content-Location"),
    CONTENT_RANGE("Content-Range"),

    // === Content Negotiation ===
    ACCEPT("Accept"),
    ACCEPT_CHARSET("Accept-Charset"),
    ACCEPT_ENCODING("Accept-Encoding"),
    ACCEPT_LANGUAGE("Accept-Language"),
    ACCEPT_RANGES("Accept-Ranges"),

    // === Authentication & Authorization ===
    AUTHORIZATION("Authorization"),
    PROXY_AUTHENTICATE("Proxy-Authenticate"),
    PROXY_AUTHORIZATION("Proxy-Authorization"),
    WWW_AUTHENTICATE("WWW-Authenticate"),

    // === Caching & Validation ===
    AGE("Age"),
    CACHE_CONTROL("Cache-Control"),
    DATE("Date"),
    ETAG("ETag"),
    EXPIRES("Expires"),
    IF_MATCH("If-Match"),
    IF_MODIFIED_SINCE("If-Modified-Since"),
    IF_NONE_MATCH("If-None-Match"),
    IF_RANGE("If-Range"),
    IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
    LAST_MODIFIED("Last-Modified"),
    VARY("Vary"),

    // === Routing & Reverse Proxy / Intermediary ===
    FORWARDED("Forwarded"),
    VIA("Via"),
    X_FORWARDED_FOR("X-Forwarded-For"),
    X_FORWARDED_HOST("X-Forwarded-Host"),
    X_FORWARDED_PROTO("X-Forwarded-Proto"),
    X_REAL_IP("X-Real-IP"),

    // === Redirection & Requests ===
    LOCATION("Location"),
    ORIGIN("Origin"),
    RANGE("Range"),
    REFERER("Referer"),
    RETRY_AFTER("Retry-After"),
    SERVER("Server"),
    USER_AGENT("User-Agent"),

    // === State Management (Cookies) ===
    COOKIE("Cookie"),
    SET_COOKIE("Set-Cookie"),

    // === CORS ===
    ACCESS_CONTROL_ALLOW_CREDENTIALS("Access-Control-Allow-Credentials"),
    ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers"),
    ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods"),
    ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
    ACCESS_CONTROL_EXPOSE_HEADERS("Access-Control-Expose-Headers"),
    ACCESS_CONTROL_MAX_AGE("Access-Control-Max-Age"),
    ACCESS_CONTROL_REQUEST_HEADERS("Access-Control-Request-Headers"),
    ACCESS_CONTROL_REQUEST_METHOD("Access-Control-Request-Method"),

    // === Security Headers ===
    CONTENT_SECURITY_POLICY("Content-Security-Policy"),
    STRICT_TRANSPORT_SECURITY("Strict-Transport-Security"),
    X_CONTENT_TYPE_OPTIONS("X-Content-Type-Options"),
    X_FRAME_OPTIONS("X-Frame-Options");

    private final String value;
    private final String lowerCaseName;

    // Fast O(1) case-insensitive lookup table populated once at startup
    private static final Map<String, HttpHeader> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    header -> header.value.toLowerCase(Locale.ROOT),
                    Function.identity()
            ));

    HttpHeader(String value) {
        this.value = value;
        this.lowerCaseName = value.toLowerCase(Locale.ROOT);
    }

    public String value() {
        return this.value;
    }

    public String lowerCaseName() {
        return this.lowerCaseName;
    }

    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Case-insensitive lookup returning Optional.
     * Zero exception overhead for unlisted/custom application headers.
     */
    public static Optional<HttpHeader> from(String rawName) {
        if (rawName == null || rawName.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(LOOKUP.get(rawName.toLowerCase(Locale.ROOT)));
    }

    /**
     * Fast O(1) presence check without generating exceptions.
     */
    public static boolean contains(String rawName) {
        if (rawName == null || rawName.isEmpty()) {
            return false;
        }
        return LOOKUP.containsKey(rawName.toLowerCase(Locale.ROOT));
    }
}