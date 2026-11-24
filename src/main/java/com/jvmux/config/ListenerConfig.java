package com.jvmux.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListenerConfig {

    @Builder.Default
    private String host = "0.0.0.0";

    @Builder.Default
    private int port = 8080;

    @Builder.Default
    private int backlog = 1024;

    @Builder.Default
    private int maxRequestsPerConnection = 1000;
}
