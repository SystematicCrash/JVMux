package com.jvmux.network.http.io;

import com.jvmux.network.http.codec.decode.RequestParser;
import com.jvmux.network.http.codec.encode.ResponseEncoder;
import com.jvmux.network.http.exception.HttpException;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HttpServer {

    public static HttpRequest readRequest(InputStream in) throws IOException {
        String[] raw = HttpHeaderReader.readHeaders(in, false);
        if (raw == null) {
            return null;
        }

        try {
            return RequestParser.parse(raw, in);
        } catch (IllegalArgumentException e) {
            throw HttpException.badRequest("Malformed client request: " + e.getMessage(), e);
        }
    }

    public static void writeResponse(HttpResponse response, OutputStream out) throws IOException {
        out.write(ResponseEncoder.encode(response));
        var bodyWriter = response.getWriter();

        if (bodyWriter.isPresent()) {
            bodyWriter.get().write(out);
        }
        out.flush();
    }
}