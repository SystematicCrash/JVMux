package com.jvmux.network.tcp;

import java.io.InputStream;
import java.io.OutputStream;

@FunctionalInterface
public interface ConnectionHandler {
    boolean handle(InputStream in, OutputStream out) throws Exception;
}