package com.jvmux.network.session;

import java.io.InputStream;
import java.io.OutputStream;

public record TrafficSide(InputStream in, OutputStream out) {}
