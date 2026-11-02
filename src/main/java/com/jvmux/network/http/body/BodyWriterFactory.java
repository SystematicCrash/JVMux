package com.jvmux.network.http.body;

import com.jvmux.network.http.constant.HttpHeader;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class BodyWriterFactory {

    private BodyWriterFactory() {}

    public static Optional<BodyWriter> create(InputStream body, Map<String, String> headers) {
        if (headers == null) {
            return Optional.empty();
        }

        if (isChunked(headers)) {
            return Optional.of(new ChunkedBodyWriter(body));
        }

        if (isFixedSize(headers)) {
            return Optional.of(new FixedBodyWriter(body, getContentLength(headers)));
        }

        return Optional.empty();
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
