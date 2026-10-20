package com.jvmux;

import com.jvmux.network.http.model.HttpServer;
import com.jvmux.network.routing.Router;
import com.jvmux.network.tcp.TCPServer;

public class Main {
    public static void main(String[] args) throws Exception {
       System.out.println("Application is up and running..."); 

        var routeTree = new Router().build();
        var httpServer = new HttpServer(routeTree);

        try (var tcpServer = new TCPServer(1234, httpServer::handle)){
            tcpServer.start();
        }
    }
}
