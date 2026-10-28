package com.jvmux.network.http.constant;

public final class HttpHeader {

    private HttpHeader() {}

    public static final String CONNECTION = "connection";
    public static final String CONTENT_LENGTH = "content-length";
    public static final String TRANSFER_ENCODING = "transfer-encoding";
    public static final String HOST = "host";
    public static final String TRAILER = "trailer";
    public static final String UPGRADE = "upgrade";

    public static final String CONTENT_TYPE = "content-type";
    public static final String CONTENT_ENCODING = "content-encoding";
    public static final String CONTENT_LANGUAGE = "content-language";
    public static final String CONTENT_LOCATION = "content-location";
    public static final String CONTENT_RANGE = "content-range";

    public static final String ACCEPT = "accept";
    public static final String ACCEPT_CHARSET = "accept-charset";
    public static final String ACCEPT_ENCODING = "accept-encoding";
    public static final String ACCEPT_LANGUAGE = "accept-language";
    public static final String ACCEPT_RANGES = "accept-ranges";

    public static final String AUTHORIZATION = "authorization";
    public static final String PROXY_AUTHENTICATE = "proxy-authenticate";
    public static final String PROXY_AUTHORIZATION = "proxy-authorization";
    public static final String WWW_AUTHENTICATE = "www-authenticate";

    public static final String AGE = "age";
    public static final String CACHE_CONTROL = "cache-control";
    public static final String DATE = "date";
    public static final String ETAG = "etag";
    public static final String EXPIRES = "expires";
    public static final String IF_MATCH = "if-match";
    public static final String IF_MODIFIED_SINCE = "if-modified-since";
    public static final String IF_NONE_MATCH = "if-none-match";
    public static final String IF_RANGE = "if-range";
    public static final String IF_UNMODIFIED_SINCE = "if-unmodified-since";
    public static final String LAST_MODIFIED = "last-modified";
    public static final String VARY = "vary";

    public static final String FORWARDED = "forwarded";
    public static final String VIA = "via";
    public static final String X_FORWARDED_FOR = "x-forwarded-for";
    public static final String X_FORWARDED_HOST = "x-forwarded-host";
    public static final String X_FORWARDED_PROTO = "x-forwarded-proto";
    public static final String X_FORWARDED_PORT = "x-forwarded-port";
    public static final String X_REAL_IP = "x-real-ip";

    public static final String LOCATION = "location";
    public static final String ORIGIN = "origin";
    public static final String RANGE = "range";
    public static final String REFERER = "referer";
    public static final String RETRY_AFTER = "retry-after";
    public static final String SERVER = "server";
    public static final String USER_AGENT = "user-agent";

    public static final String COOKIE = "cookie";
    public static final String SET_COOKIE = "set-cookie";

    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "access-control-allow-credentials";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS = "access-control-allow-headers";
    public static final String ACCESS_CONTROL_ALLOW_METHODS = "access-control-allow-methods";
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "access-control-allow-origin";
    public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "access-control-expose-headers";
    public static final String ACCESS_CONTROL_MAX_AGE = "access-control-max-age";
    public static final String ACCESS_CONTROL_REQUEST_HEADERS = "access-control-request-headers";
    public static final String ACCESS_CONTROL_REQUEST_METHOD = "access-control-request-method";

    public static final String CONTENT_SECURITY_POLICY = "content-security-policy";
    public static final String STRICT_TRANSPORT_SECURITY = "strict-transport-security";
    public static final String X_CONTENT_TYPE_OPTIONS = "x-content-type-options";
    public static final String X_FRAME_OPTIONS = "x-frame-options";
}