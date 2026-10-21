package com.jvmux.network.proxy;

import com.jvmux.network.http.exception.ErrorHandler;
import com.jvmux.network.http.exception.HttpException;
import com.jvmux.network.http.io.HttpClient;
import com.jvmux.network.http.io.HttpServer;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.routing.LookupKind;
import com.jvmux.network.routing.RouteTree;
import com.jvmux.network.tcp.TCPClient;
import com.jvmux.network.upstream.Cluster;

import java.io.IOException;
import java.net.Socket;

public class ProxyRelay implements Runnable, AutoCloseable {

    private final Socket clientSocket;
    private final RouteTree routeTree;
    private final StreamPair downstream;

    private Cluster currentCluster;
    private Socket upstreamSocket;
    private StreamPair upstream;

    public ProxyRelay(Socket clientSocket, RouteTree routeTree) throws IOException {
        this.clientSocket = clientSocket;
        this.routeTree = routeTree;
        this.downstream = new StreamPair(
                clientSocket.getInputStream(),
                clientSocket.getOutputStream()
        );
    }

    @Override
    public void run() {
        try (this) {
            connectionLoop();
        } catch (Exception ignored) {}
    }

    private void connectionLoop() throws IOException {
        while (!clientSocket.isClosed()) {
            HttpRequest request;
            try {
                request = HttpServer.readRequest(downstream.in());
                if (request == null) {
                    break;
                }
            } catch (Exception e) {
                HttpServer.writeResponse(ErrorHandler.toResponse(e), downstream.out());
                break;
            }

            try {
                var targetCluster = resolveRoute(request);
                ensureUpstreamConnection(targetCluster);

                HttpClient.writeRequest(request, upstream.out());

                var response = HttpClient.readResponse(upstream.in());
                if (response == null) {
                    throw HttpException.badGateway();
                }

                HttpServer.writeResponse(response, downstream.out());

            } catch (Exception e) {
                HttpServer.writeResponse(ErrorHandler.toResponse(e), downstream.out());
                closeUpstream();
                break;
            }
        }
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

    private void ensureUpstreamConnection(Cluster targetCluster) throws IOException {
        if (this.currentCluster != targetCluster || upstreamSocket == null || upstreamSocket.isClosed()) {
            closeUpstream();
            this.currentCluster = targetCluster;
            var server = targetCluster.getRandomServer();
            this.upstreamSocket = TCPClient.connect(server.host(), server.port());
            this.upstream = new StreamPair(
                    this.upstreamSocket.getInputStream(),
                    this.upstreamSocket.getOutputStream()
            );
        }
    }

    private void closeUpstream() {
        if (this.upstreamSocket != null) {
            try {
                this.upstreamSocket.close();
            } catch (IOException ignored) {}
            this.upstreamSocket = null;
            this.upstream = null;
        }
    }

    @Override
    public void close() throws IOException {
        closeUpstream();
        if (!clientSocket.isClosed()) {
            clientSocket.close();
        }
    }
}