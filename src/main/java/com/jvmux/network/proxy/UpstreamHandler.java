package com.jvmux.network.proxy;

import com.jvmux.network.balancer.BalancingStrategy;
import com.jvmux.network.balancer.LoadBalancer;
import com.jvmux.network.balancer.LoadBalancerFactory;
import com.jvmux.network.balancer.SelectionContext;
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
import java.util.Objects;

public class UpstreamHandler implements AutoCloseable {
    private static final int MAX_ATTEMPTS = 2;
    private static final BalancingStrategy STRATEGY = BalancingStrategy.ROUND_ROBIN;
    private static final LoadBalancer DEFAULT_LOAD_BALANCER = LoadBalancerFactory.create(STRATEGY);

    private final RouteTree routeTree;
    private final Client client;
    private final LoadBalancer loadBalancer;
    private Cluster currentCluster;
    private Socket upstreamSocket;
    private Server currentServer;
    private StreamPair pipe;

    public UpstreamHandler(RouteTree routeTree, Client clientContext, LoadBalancer loadBalancer) {
        this.routeTree = routeTree;
        this.client = clientContext;
        this.loadBalancer = Objects.requireNonNull(loadBalancer, "loadBalancer cannot be null");
    }

    public UpstreamHandler(RouteTree routeTree, Client clientContext, BalancingStrategy strategy) {
        this(routeTree, clientContext, LoadBalancerFactory.create(strategy));
    }

    public UpstreamHandler(RouteTree routeTree, Client clientContext) {
        this(routeTree, clientContext, DEFAULT_LOAD_BALANCER);
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
        request = HttpTransformer.prepareForUpstream(request, this.currentServer, this.client);
        HttpClient.writeRequest(request, pipe.out());

        long start = System.nanoTime();
        var response = HttpClient.readResponse(pipe.in());
        long responseTimeNanos = System.nanoTime() - start;

        if (response == null) {
            return null;
        }

        this.currentServer.recordResponseTime(responseTimeNanos);

        return HttpTransformer.prepareForDownstream(
                response,
                request.getHeader(HttpHeader.HOST),
                this.currentServer.getUrl()
        );
    }

    private Cluster resolveRoute(HttpRequest request) {
        var lookupResult = this.routeTree.lookup(request.getMethod(), request.getPath());
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

            var context = this.client != null
                    ? new SelectionContext(this.client.clientIp())
                    : SelectionContext.empty();

            try {
                this.currentServer = this.loadBalancer.select(targetCluster, context);
            } catch (IllegalStateException e) {
                throw HttpException.badGateway("No healthy servers in cluster: " + targetCluster.name(), e);
            }

            try {
                this.upstreamSocket = TCPClient.connect(
                        this.currentServer.getHost(),
                        this.currentServer.getPort()
                );
                this.currentServer.incConns();
                this.pipe = StreamPair.from(this.upstreamSocket);
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
        if (this.upstreamSocket != null && !this.upstreamSocket.isClosed()) {
            try {
                this.upstreamSocket.close();
            } catch (IOException ignored) {
            } finally {
                this.currentServer.decConns();
            }
        }
    }
}