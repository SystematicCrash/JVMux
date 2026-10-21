package com.jvmux.network.session;

import com.jvmux.network.http.exception.ErrorHandler;
import com.jvmux.network.http.exception.HttpException;
import com.jvmux.network.http.io.HttpClient;
import com.jvmux.network.http.io.HttpServer;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;
import com.jvmux.network.routing.LookupKind;
import com.jvmux.network.routing.RouteTree;
import com.jvmux.network.tcp.TCPClient;
import com.jvmux.network.upstream.Cluster;

import java.io.IOException;
import java.net.Socket;

public class ConnectionSession implements Runnable, AutoCloseable {
    private Cluster cluster;
    private Socket serverSocket;
    private TrafficSide upstream;

    private final Socket clientSocket;
    private final RouteTree routeTree;
    private final TrafficSide downstream;

    public ConnectionSession(Socket clientSocket, RouteTree routeTree) throws IOException {
        this.clientSocket = clientSocket;
        this.downstream = new TrafficSide(
                clientSocket.getInputStream(),
                clientSocket.getOutputStream()
        );
        this.routeTree = routeTree;
    }

    @Override
    public void run() {
        while (true) {
            try {
                var request = readRequest();
                if (request == null) {
                    break;
                }

                dispatch(request);
                writeRequest(request);
                writeResponse(readResponse());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private HttpRequest readRequest() throws IOException {
        return HttpServer.readRequest(this.downstream.in());
    }

    private void writeResponse(HttpResponse response) throws IOException {
        HttpServer.writeResponse(response, this.downstream.out());
    }

    private HttpResponse readResponse() throws IOException {
        var response = HttpClient.readResponse(this.upstream.in());
        if (response == null) {
            throw HttpException.badGateway();
        }
        return response;
    }

    private void writeRequest(HttpRequest request) throws IOException {
        HttpClient.writeRequest(request, this.upstream.out());
    }

    private void openNewSocket() throws IOException {
        var server = this.cluster.getRandomServer();
        this.serverSocket = TCPClient.connect(server.host(), server.port());
        this.upstream = new TrafficSide(
                this.serverSocket.getInputStream(),
                this.serverSocket.getOutputStream()
        );
    }

    private void dispatch(HttpRequest request) throws IOException {
        var lookupResult = this.routeTree.lookup(request.method(), request.path());

        switch (lookupResult.kind()) {
            case LookupKind.NOT_FOUND -> throw HttpException.notFound();
            case LookupKind.METHOD_NOT_ALLOWED -> throw HttpException.methodNotAllowed();
        }

        if (lookupResult.cluster() != this.cluster) {
            this.cluster = lookupResult.cluster();
            openNewSocket();
        }
    }

    @Override
    public void close() throws Exception {
        this.clientSocket.close();

        if (this.serverSocket != null) {
            this.serverSocket.close();
        }
    }
}