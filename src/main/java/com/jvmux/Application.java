package com.jvmux;

import com.jvmux.network.routing.Router;
import com.jvmux.network.proxy.ProxyRelay;
import com.jvmux.network.tcp.TCPServer;

import java.util.concurrent.Executors;

public class Application {
    public static void main(String[] args) throws Exception {
        System.out.println("Application is up and running...");

        var tcpServer = new TCPServer(8080);
        var executor = Executors.newVirtualThreadPerTaskExecutor();

        while (!tcpServer.isClosed()) {
            var socket = tcpServer.accept();
            executor.submit(() -> {
                try(var session = new ProxyRelay(socket, new Router().build())) {
                    session.run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
