package com.jvmux;

import com.jvmux.network.session.ConnectionSession;
import com.jvmux.network.tcp.TCPServer;

import java.util.concurrent.Executors;

public class Application {
    public static void main(String[] args) throws Exception {
       System.out.println("Application is up and running..."); 

       var tcpServer = new TCPServer(1234);
       var executor = Executors.newVirtualThreadPerTaskExecutor();

       while (!tcpServer.isClosed()) {
            executor.submit(ConnectionSession.from(tcpServer.accept()));
       }
    }
}
