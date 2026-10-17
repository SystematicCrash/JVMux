package com.jvmux;

import com.jvmux.network.http.constant.HttpMethod;
import com.jvmux.network.http.HttpServer;
import com.jvmux.network.routing.Router;
import com.jvmux.network.routing.Handler;
import com.jvmux.network.tcp.TCPServer;

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
