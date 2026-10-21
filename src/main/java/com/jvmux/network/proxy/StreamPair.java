package com.jvmux.network.proxy;

import java.io.InputStream;
import java.io.OutputStream;

public record StreamPair(InputStream in, OutputStream out) {}
