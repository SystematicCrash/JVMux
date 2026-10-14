package com.jvmux.core.http.constant;

import java.util.Set;
import java.util.regex.Pattern;

public final class HttpConstants {

    private HttpConstants() {}

    // Primitives for fast byte-level stream inspection
    public static final char CR = '\r';
    public static final char LF = '\n';
    public static final char SP = ' ';
    public static final char COLON = ':';

    // Strings for header parsing, building responses, and splitting
    public static final String CRLF = "\r\n";

    // limitations
    public static final int MAX_HEADER_COUNT = 100;
    public static final int MAX_HEADER_SIZE = 8 * 1024;
    public static final int MAX_REQUEST_LINE_LEN = 8 * 1024;

    // validation
    public static final Pattern REQUEST_LINE_PATTERN = Pattern.compile(
            "^(?<method>[!#$%&'*+\\-.^_`|~0-9a-zA-Z]+) (?<target>[\\x21-\\x7E]+) HTTP/(?<version>\\d+\\.\\d+)$"
    );
    public static final Set<String> VALID_METHODS = Set.of(
            HttpMethod.GET.toString(),
            HttpMethod.HEAD.toString(),
            HttpMethod.OPTIONS.toString(),
            HttpMethod.DELETE.toString(),
            HttpMethod.POST.toString(),
            HttpMethod.PATCH.toString()
    );
    public static final Set<String> VALID_VERSIONS = Set.of(
            HttpVersion.HTTP_1_1.toString()
    );
    public static final Set<String> UNIQUE_HEADERS = Set.of(
            HttpHeader.HOST.toString(),
            HttpHeader.CONTENT_LENGTH.toString(),
            HttpHeader.TRANSFER_ENCODING.toString()
    );
    public static final Set<String> MANDATORY_HEADERS = Set.of(
            HttpHeader.HOST.toString()
    );
}
