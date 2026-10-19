package com.jvmux;

import com.jvmux.network.http.HttpServer;
import com.jvmux.network.routing.Router;
import com.jvmux.network.routing.Handler;
import com.jvmux.network.tcp.TCPServer;
import static com.jvmux.network.http.constant.HttpMethod.*;

public class Main {
    public static void main(String[] args) throws Exception {
       System.out.println("Application is up and running..."); 

        var routeTree = registerRoutes().build();
        var httpServer = new HttpServer(routeTree);
        var tcpServer = new TCPServer(1234, httpServer::handle);

        tcpServer.start();
    }

    private static Router registerRoutes() {
        var router = new Router();

        router.add("/users/profile", new Handler(), GET, DELETE, POST);

        return router;
    }
}
