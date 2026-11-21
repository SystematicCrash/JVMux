package com.jvmux.network.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MockUpstreamServer implements AutoCloseable {
    private final String id;
    private final ServerSocket serverSocket;
    private final ExecutorService executor;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final AtomicBoolean shouldFail = new AtomicBoolean(false);
    private volatile long delayMs = 0;
    private volatile boolean running = true;

    public MockUpstreamServer(String id) throws IOException {
        this.id = id;
        this.serverSocket = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        start();
    }

    private void start() {
        executor.submit(() -> {
            while (running && !serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    executor.submit(() -> handle(socket));
                } catch (IOException e) {
                    if (!running) break;
                }
            }
        });
    }

    private void handle(Socket socket) {
        try (socket;
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            if (shouldFail.get()) {
                // Immediately close connection to simulate an upstream failure
                return;
            }

            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ignored) {}
            }

            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) != -1) {
                buf.write(b);
                byte[] bytes = buf.toByteArray();
                if (bytes.length >= 4 &&
                        bytes[bytes.length - 4] == '\r' &&
                        bytes[bytes.length - 3] == '\n' &&
                        bytes[bytes.length - 2] == '\r' &&
                        bytes[bytes.length - 1] == '\n') {
                    break;
                }
            }

            if (buf.size() == 0) return;

            requestCount.incrementAndGet();

            String body = "response from " + id;
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: " + body.length() + "\r\n" +
                    "X-Server-Id: " + id + "\r\n" +
                    "Connection: close\r\n\r\n" +
                    body;

            out.write(response.getBytes(StandardCharsets.US_ASCII));
            out.flush();
        } catch (IOException ignored) {}
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return "127.0.0.1";
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public int getRequestCount() {
        return requestCount.get();
    }

    public void setShouldFail(boolean fail) {
        this.shouldFail.set(fail);
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }

    @Override
    public void close() throws IOException {
        running = false;
        serverSocket.close();
        executor.shutdownNow();
    }
}
