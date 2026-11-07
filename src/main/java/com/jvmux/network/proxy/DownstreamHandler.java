package com.jvmux.network.proxy;

import com.jvmux.network.http.constant.Constants;
import com.jvmux.network.http.exception.ErrorHandler;
import com.jvmux.network.http.io.HttpServer;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;

import java.io.IOException;
import java.net.Socket;

public class DownstreamHandler implements AutoCloseable {
    private final Socket clientSocket;
    private final StreamPair pipe;
    private int requestCount = 0;
    private boolean wannaClose = false;

    public DownstreamHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.pipe = StreamPair.from(this.clientSocket);
    }

    public HttpRequest receive() {
        try {
            var request = HttpServer.readRequest(pipe.in());
            if (request != null) {
                requestCount++;
                wannaClose = request.isCloseConnection();
            }
            return request;
        } catch (Exception e) {
            forwardErr(e);
            return null;
        }
    }

    public void forward(HttpResponse response) {
        boolean last = wannaClose || requestCount == Constants.MAX_REQUESTS;

        if (last) response.setAsLast();

        try {
            HttpServer.writeResponse(response, pipe.out());
        } catch (IOException e) {
            close();
        }

        if (last) close();
    }

    public void forwardErr(Throwable error) {
        forward(ErrorHandler.toResponse(error));
    }

    public boolean isConnected() {
        return !clientSocket.isClosed();
    }

    @Override
    public void close() {
        if (!clientSocket.isClosed()) {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }
}