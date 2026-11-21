package com.jvmux.network.proxy;

import com.jvmux.network.balancer.BalancingStrategy;
import com.jvmux.network.balancer.LoadBalancer;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;
import com.jvmux.network.model.Client;
import com.jvmux.network.routing.RouteTree;

import java.io.IOException;
import java.net.Socket;

public class ProxyRelay implements Runnable, AutoCloseable {

    private final UpstreamHandler upstream;
    private final DownstreamHandler downstream;

    public ProxyRelay(Socket clientSocket, RouteTree routeTree) throws IOException {
        this.downstream = new DownstreamHandler(clientSocket);
        this.upstream = new UpstreamHandler(routeTree, Client.from(clientSocket));
    }

    public ProxyRelay(Socket clientSocket, RouteTree routeTree, LoadBalancer loadBalancer) throws IOException {
        this.downstream = new DownstreamHandler(clientSocket);
        this.upstream = new UpstreamHandler(routeTree, Client.from(clientSocket), loadBalancer);
    }

    public ProxyRelay(Socket clientSocket, RouteTree routeTree, BalancingStrategy strategy) throws IOException {
        this.downstream = new DownstreamHandler(clientSocket);
        this.upstream = new UpstreamHandler(routeTree, Client.from(clientSocket), strategy);
    }

    @Override
    public void run() {
        try (this) {
            connectionLoop();
        } catch (Exception ignored) {}
    }

    private void connectionLoop() {
        while (downstream.isConnected()) {
            HttpRequest request = downstream.receive();

            if (request == null) {
                break;
            }

            try {
                HttpResponse response = upstream.forward(request);
                downstream.forward(response);

                if (response.isCloseConnection()) {
                    upstream.close();
                }
            } catch (Exception e) {
                downstream.forwardErr(e);
                break;
            }
        }
    }

    @Override
    public void close() {
        try {
            upstream.close();
        } finally {
            downstream.close();
        }
    }
}