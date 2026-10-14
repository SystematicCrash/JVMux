package com.jvmux;

import com.jvmux.core.http.HttpMethod;
import com.jvmux.core.http.HttpServer;
import com.jvmux.core.routing.Router;
import com.jvmux.core.routing.Handler;
import com.jvmux.core.tcp.ConnectionHandler;
import com.jvmux.core.tcp.TCPServer;

import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        var router = new Router();

        router.add(Set.of(HttpMethod.GET), "/users/profile", new Handler());
        router.add(Set.of(HttpMethod.POST), "/users/profile", new Handler());
        router.add(Set.of(HttpMethod.DELETE), "/users/profile", new Handler());

        var routeTree = router.build();
        var httpServer = new HttpServer(routeTree);
        var connHandler = new ConnectionHandler(httpServer);
        var tcpServer = new TCPServer(1234, connHandler);

        tcpServer.start();
    }
}
