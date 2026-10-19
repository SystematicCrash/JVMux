package com.jvmux.network.http.io;

import com.jvmux.network.http.constant.HttpHeader;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public final class BodyWriterFactory {

    private BodyWriterFactory() {}

    public static BodyWriter create(InputStream body, Map<String, String> headers) {
        if (headers == null) {
            return null;
        }

        if (isChunked(headers)) {
            return new ChunkedBodyWriter(body);
        }

        if (isFixedSize(headers)) {
            return new FixedBodyWriter(body, getContentLength(headers));
        }

        return null;
    }

    private static boolean isChunked(Map<String, String> headers) {
        return Objects.equals(headers.get(HttpHeader.TRANSFER_ENCODING), "chunked");
    }

    private static boolean isFixedSize(Map<String, String> headers) {
        return headers.get(HttpHeader.CONTENT_LENGTH) != null;
    }

    private static long getContentLength(Map<String, String> headers) {
        var contentLen = headers.get(HttpHeader.CONTENT_LENGTH);
        return Long.parseLong(contentLen);
    }
}
