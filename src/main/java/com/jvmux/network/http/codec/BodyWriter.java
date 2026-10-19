package com.jvmux.network.http.codec;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@AllArgsConstructor
public abstract class BodyWriter {
    protected final InputStream inputStream;

    public abstract void write(OutputStream clientOut) throws IOException;
}
