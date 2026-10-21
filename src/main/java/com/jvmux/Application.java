package com.jvmux;

import com.jvmux.network.routing.Router;
import com.jvmux.network.session.ConnectionSession;
import com.jvmux.network.tcp.TCPServer;

import java.util.concurrent.Executors;

public class Application {
    public static void main(String[] args) throws Exception {
        System.out.println("Application is up and running...");

        var tcpServer = new TCPServer(8080);
        var executor = Executors.newVirtualThreadPerTaskExecutor();

        while (!tcpServer.isClosed()) {
            executor.submit(() -> {
                try(var session = new ConnectionSession(tcpServer.accept(), new Router().build())) {
                    session.run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
