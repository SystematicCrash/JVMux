package com.jvmux.network.http.constant;

import java.util.Set;

public final class Constants {

    private Constants() {}

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
    public static final int READ_BUFFER_SIZE = 8 * 1024;

    // validation
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
            HttpHeader.HOST,
            HttpHeader.CONTENT_LENGTH,
            HttpHeader.TRANSFER_ENCODING
    );
    public static final Set<String> MANDATORY_HEADERS = Set.of(
            HttpHeader.HOST
    );
}
