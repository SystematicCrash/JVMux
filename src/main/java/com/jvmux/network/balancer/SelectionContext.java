package com.jvmux.network.balancer;

public record SelectionContext(String clientIp) {
    public static SelectionContext empty() {
        return new SelectionContext(null);
    }
}