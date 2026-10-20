package com.jvmux.network.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@FunctionalInterface
public interface ConnectionProcessor<T> {
    T process(InputStream in, OutputStream out) throws IOException;
}