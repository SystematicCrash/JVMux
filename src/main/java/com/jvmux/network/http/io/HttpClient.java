package com.jvmux.network.http.io;

import com.jvmux.network.http.codec.decode.ResponseParser;
import com.jvmux.network.http.codec.encode.RequestEncoder;
import com.jvmux.network.http.exception.HttpException;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HttpClient {

    public static void writeRequest(HttpRequest request, OutputStream out) throws IOException {
        out.write(RequestEncoder.encode(request));
        var bodyWriter = request.getWriter();

        if (bodyWriter.isPresent()) {
            bodyWriter.get().write(out);
        }
        out.flush();
    }

    public static HttpResponse readResponse(InputStream in) throws IOException {
        String[] raw = HttpHeaderReader.readHeaders(in, true);
        if (raw == null) {
            return null;
        }

        try {
            return ResponseParser.parse(raw, in);
        } catch (IllegalArgumentException e) {
            throw HttpException.badGateway("Malformed upstream response: " + e.getMessage(), e);
        }
    }
}