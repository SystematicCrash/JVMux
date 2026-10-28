package com.jvmux.network.proxy;

import com.jvmux.network.http.exception.ErrorHandler;
import com.jvmux.network.http.io.HttpServer;
import com.jvmux.network.http.model.HttpRequest;
import com.jvmux.network.http.model.HttpResponse;

import java.io.IOException;
import java.net.Socket;

public class DownstreamHandler implements AutoCloseable {

    private final Socket clientSocket;
    private final StreamPair pipe;

    public DownstreamHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.pipe = StreamPair.from(this.clientSocket);
    }

    public HttpRequest receive() throws IOException {
        try {
            return HttpServer.readRequest(pipe.in());
        } catch (Exception e) {
            forwardErr(e);
            return null;
        }
    }

    public void forward(HttpResponse response) throws IOException {
        HttpServer.writeResponse(response, pipe.out());
    }

    public void forwardErr(Throwable error) throws IOException {
        HttpServer.writeResponse(ErrorHandler.toResponse(error), pipe.out());
    }

    public boolean isConnected() {
        return !clientSocket.isClosed();
    }

    @Override
    public void close() throws IOException {
        if (!clientSocket.isClosed()) {
            clientSocket.close();
        }
    }
}