package com.jvmux.network.proxy;

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

    @Override
    public void run() {
        try (this) {
            connectionLoop();
        } catch (Exception ignored) {}
    }

    private void connectionLoop() throws IOException {
        while (downstream.isConnected()) {
            HttpRequest request = downstream.receive();
            if (request == null) {
                break;
            }

            try {
                HttpResponse response = upstream.forward(request);

                boolean clientClose = request.isCloseConnection();

                if (clientClose) {
                    response.setAsLast();
                }
                downstream.forward(response);

                boolean upstreamClose = response.isCloseConnection();

                if (upstreamClose) upstream.close();
                if (clientClose) break;

            } catch (Exception e) {
                downstream.forwardErr(e);
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