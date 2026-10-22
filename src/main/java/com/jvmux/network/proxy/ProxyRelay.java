package com.jvmux.network.proxy;

import com.jvmux.network.http.constant.HttpHeader;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;
import com.jvmux.network.routing.RouteTree;

import java.io.IOException;
import java.net.Socket;

public class ProxyRelay implements Runnable, AutoCloseable {

    private final DownstreamHandler downstream;
    private final UpstreamHandler upstream;

    public ProxyRelay(Socket clientSocket, RouteTree routeTree) throws IOException {
        this.downstream = new DownstreamHandler(clientSocket);
        this.upstream = new UpstreamHandler(routeTree);
    }

    @Override
    public void run() {
        try (this) {
            connectionLoop();
        } catch (Exception ignored) {}
    }

    private void connectionLoop() throws IOException {
        while (downstream.isConnected()) {
            HttpRequest request = downstream.readRequest();
            if (request == null) {
                break;
            }

            try {
                HttpResponse response = upstream.forward(request);
                downstream.writeResponse(response);
                boolean upstreamClose = "close".equalsIgnoreCase(response.getHeader(HttpHeader.CONNECTION));
                boolean clientClose = "close".equalsIgnoreCase(request.getHeader(HttpHeader.CONNECTION));

                if (upstreamClose) {
                    upstream.close();
                }
                if (clientClose) {
                    break;
                }

            } catch (Exception e) {
                downstream.writeError(e);
                upstream.close();
                break;
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            upstream.close();
        } finally {
            downstream.close();
        }
    }
}