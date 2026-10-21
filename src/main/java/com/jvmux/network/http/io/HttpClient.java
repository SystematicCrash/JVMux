package com.jvmux.network.http.io;

import com.jvmux.network.http.codec.RequestEncoder;
import com.jvmux.network.http.codec.ResponseParser;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;

import java.io.*;

public class HttpClient {

    public static void writeRequest(HttpRequest request, OutputStream out) throws IOException {
        out.write(RequestEncoder.encode(request));
    }

    public static HttpResponse readResponse(InputStream in) throws IOException {
        String[] raw = HttpHeaderReader.readHeaders(in, true);
        return (raw == null) ? null : ResponseParser.parse(raw, in);
    }
}