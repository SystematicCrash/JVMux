package com.jvmux;

import com.jvmux.net.http.constant.HttpMethod;
import com.jvmux.net.http.HttpServer;
import com.jvmux.net.routing.Router;
import com.jvmux.net.routing.Handler;
import com.jvmux.net.tcp.TCPServer;

import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
       System.out.println("Application is up and running..."); 
       var router = new Router();

        router.add(Set.of(HttpMethod.GET), "/users/profile", new Handler());
        router.add(Set.of(HttpMethod.POST), "/users/profile", new Handler());
        router.add(Set.of(HttpMethod.DELETE), "/users/profile", new Handler());

        var routeTree = router.build();
        var httpServer = new HttpServer(routeTree);
        var tcpServer = new TCPServer(1234, httpServer::handle);

        tcpServer.start();
    }
}
