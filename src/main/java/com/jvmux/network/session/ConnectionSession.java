package com.jvmux.network.session;

import com.jvmux.network.inbound.InboundHandler;
import com.jvmux.network.outbound.OutboundHandler;

public class ConnectionSession implements Runnable {
    private InboundHandler inboundHandler;
    private OutboundHandler outboundHandler;

    @Override
    public void run() {
        // TODO: Implement whole flow
    }
}
