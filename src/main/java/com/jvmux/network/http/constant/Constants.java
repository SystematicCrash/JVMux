package com.jvmux.network.http.constant;

import java.util.Set;
import java.util.stream.Collectors;

public final class Constants {

    private Constants() {}

    // Primitives for fast byte-level stream inspection
    public static final char CR = '\r';
    public static final char LF = '\n';
    public static final char SP = ' ';
    public static final char COLON = ':';
    public static final char SEMI_COLON = ';';

    // Strings for header parsing, building responses, and splitting
    public static final String CRLF = "\r\n";

    // limitations
    public static final int MAX_HEADER_COUNT = 100;
    public static final int MAX_HEADER_SIZE = 8 * 1024;
    public static final int MAX_REQUEST_LINE_LEN = 8 * 1024;
    public static final int READ_BUFFER_SIZE = 8 * 1024;

    // validation
    public static final Set<String> VALID_METHODS = Set.of(
            HttpMethod.GET.value(),
            HttpMethod.HEAD.value(),
            HttpMethod.OPTIONS.value(),
            HttpMethod.DELETE.value(),
            HttpMethod.POST.value(),
            HttpMethod.PATCH.value()
    );
    public static final Set<String> SUPPORTED_VERSIONS = Set.of(
            HttpVersion.HTTP_1_1.value()
    );
    public static final Set<String> VALID_VERSIONS = Set.of(
            HttpVersion.HTTP_1_0.value(),
            HttpVersion.HTTP_1_1.value(),
            HttpVersion.HTTP_2.value(),
            HttpVersion.HTTP_3.value()
    );
    public static final Set<String> UNIQUE_HEADERS = Set.of(
            HttpHeader.HOST,
            HttpHeader.CONTENT_LENGTH,
            HttpHeader.TRANSFER_ENCODING,
            HttpHeader.CONTENT_TYPE,
            HttpHeader.AUTHORIZATION,
            HttpHeader.USER_AGENT,
            HttpHeader.REFERER,
            HttpHeader.DATE,
            HttpHeader.LOCATION,
            HttpHeader.ETAG,
            HttpHeader.LAST_MODIFIED,
            HttpHeader.RETRY_AFTER,
            HttpHeader.SERVER
    );

    public static final Set<String> REQUEST_MANDATORY_HEADERS = Set.of(
            HttpHeader.HOST,
            HttpHeader.CONTENT_TYPE
    );

    public static final Set<String> RESPONSE_MANDATORY_HEADERS = Set.of(
            HttpHeader.DATE
    );

    public static final Set<String> PAYLOAD_SPECIFIER_HEADERS = Set.of(
            HttpHeader.CONTENT_LENGTH,
            HttpHeader.TRANSFER_ENCODING
    );
}
