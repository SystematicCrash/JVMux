package com.jvmux.network.proxy;

import com.jvmux.network.http.constant.HttpHeader;
import com.jvmux.network.http.exception.HttpException;
import com.jvmux.network.http.io.HttpClient;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;
import com.jvmux.network.routing.LookupKind;
import com.jvmux.network.routing.RouteTree;
import com.jvmux.network.tcp.TCPClient;
import com.jvmux.network.model.Client;
import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

public class UpstreamHandler implements AutoCloseable {
    private static final int MAX_ATTEMPTS = 2;
    private final RouteTree routeTree;
    private Cluster currentCluster;
    private Socket upstreamSocket;
    private Server currentServer;
    private final Client client;
    private StreamPair pipe;

    public UpstreamHandler(RouteTree routeTree, Client clientContext) {
        this.routeTree = routeTree;
        this.client = clientContext;
    }

    public HttpResponse forward(HttpRequest request) {
        Cluster targetCluster = resolveRoute(request);
        return forwardWithRetry(targetCluster, request);
    }

    private HttpResponse forwardWithRetry(Cluster targetCluster, HttpRequest request) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            ensureConnection(targetCluster);

            try {
                HttpResponse response = sendAndReceive(request);
                if (response != null) {
                    return response;
                }
            } catch (IOException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw HttpException.badGateway("Failed communicating with upstream backend", e);
                }
            }

            close();
        }

        throw HttpException.badGateway();
    }

    private HttpResponse sendAndReceive(HttpRequest request) throws IOException {
        request = prepareForUpstream(request, this.currentServer);
        HttpClient.writeRequest(request, pipe.out());
        return HttpClient.readResponse(pipe.in());
    }

    private HttpRequest prepareForUpstream(HttpRequest request, Server server) {
        var headers = new HashMap<>(request.headers());

        String originalHost = headers.get(HttpHeader.HOST);
        if (originalHost != null && !headers.containsKey(HttpHeader.X_FORWARDED_HOST)) {
            headers.put(HttpHeader.X_FORWARDED_HOST, originalHost);
        }

        prepareHeaders(headers, server);

        return HttpRequest.builder()
                .method(request.method())
                .path(request.path())
                .version(request.version())
                .headers(headers)
                .body(request.body())
                .build();
    }

    private void prepareHeaders(Map<String, String> headers, Server server) {
        var targetHost = (server.port() == 80 || server.port() == 443)
                ? server.host()
                : server.host() + ":" + server.port();
        headers.put(HttpHeader.HOST, targetHost);

        var forwardedFor = !headers.containsKey(HttpHeader.X_FORWARDED_FOR)
                ? client.clientIp()
                : headers.get(HttpHeader.X_FORWARDED_FOR) + ", " + client.clientIp();
        headers.put(HttpHeader.X_FORWARDED_FOR, forwardedFor);

        var originalHost = headers.get(HttpHeader.HOST);
        if (originalHost != null && !headers.containsKey(HttpHeader.X_FORWARDED_HOST)) {
            headers.put(HttpHeader.X_FORWARDED_HOST, originalHost);
        }

        headers.put(HttpHeader.X_FORWARDED_PROTO, "http");
        headers.put(HttpHeader.X_FORWARDED_PORT, String.valueOf(client.localPort()));
    }

    private Cluster resolveRoute(HttpRequest request) {
        var lookupResult = this.routeTree.lookup(request.method(), request.path());
        if (lookupResult.kind() == LookupKind.NOT_FOUND) {
            throw HttpException.notFound();
        }
        if (lookupResult.kind() == LookupKind.METHOD_NOT_ALLOWED) {
            throw HttpException.methodNotAllowed();
        }
        return lookupResult.cluster();
    }

    private void ensureConnection(Cluster targetCluster) {
        if (this.currentCluster != targetCluster || upstreamSocket == null || upstreamSocket.isClosed()) {
            close();
            this.currentCluster = targetCluster;
            this.currentServer = targetCluster.getRandomServer();

            try {
                this.upstreamSocket = TCPClient.connect(
                        this.currentServer.host(), this.currentServer.port()
                );
                this.pipe = new StreamPair(
                        this.upstreamSocket.getInputStream(),
                        this.upstreamSocket.getOutputStream()
                );
            } catch (SocketTimeoutException e) {
                throw HttpException.gatewayTimeout(e);
            } catch (IOException e) {
                throw HttpException.badGateway(
                        "Failed to connect to upstream server ", e
                );
            }
        }
    }

    @Override
    public void close() {
        if (this.upstreamSocket != null) {
            try {
                this.upstreamSocket.close();
            } catch (IOException ignored) {}
            this.upstreamSocket = null;
            this.pipe = null;
        }
    }
}