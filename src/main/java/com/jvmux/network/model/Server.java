package com.jvmux.network.model;

public record Server(String host, int port) {
    public String url() {
        return (port == 80 || port == 443)
                ? host
                : host + ":" + port;
    }
}
