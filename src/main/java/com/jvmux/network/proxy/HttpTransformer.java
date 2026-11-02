package com.jvmux.network.proxy;

import com.jvmux.network.http.codec.format.HeaderFormatter;
import com.jvmux.network.http.constant.HttpHeader;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;
import com.jvmux.network.model.Client;
import com.jvmux.network.model.Server;

import java.util.HashMap;

public class HttpTransformer {

    public static HttpRequest prepareForUpstream(HttpRequest request, Server server, Client client) {
        var headers = new HashMap<>(request.getHeaders());

        headers.put(HttpHeader.HOST, server.url());
        headers.put(HttpHeader.X_FORWARDED_FOR, HeaderFormatter.formatForwardedFor(
                request.getHeader(HttpHeader.X_FORWARDED_FOR),
                client.clientIp()
        ));

        var originalHost = request.getHeader(HttpHeader.HOST);
        if (originalHost != null && !request.hasHeader(HttpHeader.X_FORWARDED_HOST)) {
            headers.put(HttpHeader.X_FORWARDED_HOST, originalHost);
        }

        headers.put(HttpHeader.X_FORWARDED_PROTO, "http");
        headers.put(HttpHeader.X_FORWARDED_PORT, String.valueOf(client.localPort()));

        return HttpRequest.builder()
                .method(request.getMethod())
                .path(request.getPath())
                .version(request.getVersion())
                .headers(headers)
                .body(request.getBody())
                .build();
    }

    public static HttpResponse prepareForDownstream(HttpResponse response, String requestHost, String upstreamUrl) {
        response.getHeaders().remove(HttpHeader.KEEP_ALIVE);
        response.getHeaders().remove(HttpHeader.UPGRADE);
        response.getHeaders().remove(HttpHeader.PROXY_AUTHENTICATE);
        response.getHeaders().remove(HttpHeader.PROXY_AUTHORIZATION);
        response.getHeaders().remove(HttpHeader.TRAILER);
        response.getHeaders().remove(HttpHeader.TE);

        HeaderFormatter.formatLocation(
                response.getHeader(HttpHeader.LOCATION),
                upstreamUrl,
                requestHost
        ).ifPresent((loc) -> response.setHeader(HttpHeader.LOCATION, loc));

        return response;
    }
}
