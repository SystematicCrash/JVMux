package com.jvmux.core.http;

public final class HttpConstants {

    private HttpConstants() {}

    // Primitives for fast byte-level stream inspection
    public static final byte CR_BYTE = 13;
    public static final byte LF_BYTE = 10;
    public static final char CR = '\r';
    public static final char LF = '\n';

    // Strings for header parsing, building responses, and splitting
    public static final String CRLF = "\r\n";
    public static final String HEADER_TERMINATOR = "\r\n\r\n";
    public static final String COLON = ":";
    public static final String SP = " ";

    // limitations
    public static final int MAX_HEADER_SIZE = 8 * 1024;
}
