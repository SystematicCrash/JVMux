package com.jvmux.network.proxy;

import com.jvmux.network.http.exception.HttpException;
import com.jvmux.network.http.io.HttpClient;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;
import com.jvmux.network.routing.LookupKind;
import com.jvmux.network.routing.RouteTree;
import com.jvmux.network.tcp.TCPClient;
import com.jvmux.network.upstream.Cluster;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class UpstreamHandler implements AutoCloseable {

    private final RouteTree routeTree;
    private Cluster currentCluster;
    private Socket upstreamSocket;
    private StreamPair pipe;

    public UpstreamHandler(RouteTree routeTree) {
        this.routeTree = routeTree;
    }

    public HttpResponse forward(HttpRequest request) throws IOException {
        Cluster targetCluster = resolveRoute(request);
        return forwardWithRetry(targetCluster, request);
    }

    private HttpResponse forwardWithRetry(Cluster targetCluster, HttpRequest request) throws IOException {
        ensureConnection(targetCluster);

        try {
            HttpClient.writeRequest(request, pipe.out());
            HttpResponse response = HttpClient.readResponse(pipe.in());
            if (response != null) {
                return response;
            }
        } catch (IOException ignored) {}

        close();
        ensureConnection(targetCluster);

        HttpClient.writeRequest(request, pipe.out());
        HttpResponse response = HttpClient.readResponse(pipe.in());
        if (response == null) {
            throw HttpException.badGateway();
        }
        return response;
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
            var server = targetCluster.getRandomServer();

            try {
                this.upstreamSocket = TCPClient.connect(server.host(), server.port());
                this.pipe = new StreamPair(
                        this.upstreamSocket.getInputStream(),
                        this.upstreamSocket.getOutputStream()
                );
            } catch (SocketTimeoutException e) {
                throw HttpException.gatewayTimeout(e);
            } catch (IOException e) {
                throw HttpException.badGateway("Failed to connect to backend " + server.host() + ":" + server.port(), e);
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