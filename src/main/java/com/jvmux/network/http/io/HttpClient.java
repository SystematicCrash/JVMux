package com.jvmux.network.http.io;

import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;

import java.io.InputStream;
import java.io.OutputStream;

public class HttpClient {

    public static void writeRequest(HttpRequest request, OutputStream out) {

    }

    public static HttpResponse readResponse(InputStream in) {
        return HttpResponse.text(200, "Hello Bitch");
    }
}
