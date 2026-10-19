package com.jvmux;

import com.jvmux.net.http.HttpServer;
import com.jvmux.net.http.constant.HttpMethod;
import com.jvmux.net.routing.Router;
import com.jvmux.net.routing.Handler;
import com.jvmux.net.tcp.TCPServer;

public class Main {
    public static void main(String[] args) throws Exception {
       System.out.println("Application is up and running..."); 
       var router = new Router();

        router.add("/users/profile", new Handler(),
                HttpMethod.GET, HttpMethod.DELETE, HttpMethod.POST
        );

        var routeTree = router.build();
        var httpServer = new HttpServer(routeTree);
        var tcpServer = new TCPServer(1234, httpServer::handle);

        tcpServer.start();
    }
}
