package com.jvmux.network.http.io;

import com.jvmux.network.http.codec.encode.RequestEncoder;
import com.jvmux.network.http.codec.decode.ResponseParser;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;

import java.io.*;

import static com.jvmux.network.http.constant.Constants.CRLF;

public class HttpClient {

    public static void writeRequest(HttpRequest request, OutputStream out) throws IOException {
        out.write(RequestEncoder.encode(request));
        var bodyWriter = request.getWriter();

        if (bodyWriter.isPresent()) bodyWriter.get().write(out);
        else out.write(CRLF.getBytes());
    }

    public static HttpResponse readResponse(InputStream in) throws IOException {
        String[] raw = HttpHeaderReader.readHeaders(in, true);
        return (raw == null) ? null : ResponseParser.parse(raw, in);
    }
}