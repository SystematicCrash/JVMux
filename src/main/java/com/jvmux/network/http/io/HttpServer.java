package com.jvmux.network.http.io;

import com.jvmux.network.http.codec.decode.RequestParser;
import com.jvmux.network.http.codec.encode.ResponseEncoder;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;

import java.io.*;

public class HttpServer {

    public static HttpRequest readRequest(InputStream in) throws IOException {
        String[] raw = HttpHeaderReader.readHeaders(in, false);
        return (raw == null) ? null : RequestParser.parse(raw, in);
    }

    public static void writeResponse(HttpResponse response, OutputStream out) throws IOException {
        out.write(ResponseEncoder.encode(response));
        var bodyWriter = response.getWriter();

        if (bodyWriter.isPresent()) {
            bodyWriter.get().write(out);
        }
    }
}