package com.jvmux.network.session;

import com.jvmux.network.inbound.InboundHandler;
import com.jvmux.network.outbound.OutboundHandler;

import java.io.IOException;
import java.net.Socket;

public class ConnectionSession implements Runnable {
    private InboundHandler inboundHandler;
    private OutboundHandler outboundHandler;

    public ConnectionSession(InboundHandler inboundHandler) {
        this.inboundHandler = inboundHandler;
    }

    public static ConnectionSession from(Socket clientSocket) throws IOException {
        return new ConnectionSession(InboundHandler.from(clientSocket));
    }

    @Override
    public void run() {
        // TODO: Implement whole flow
    }
}
