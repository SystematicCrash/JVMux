package com.jvmux.config;

import com.jvmux.network.model.Server;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerConfig {

    @Builder.Default
    private String host = "127.0.0.1";

    @Builder.Default
    private int port = 80;

    @Builder.Default
    private int weight = 1;

    @Builder.Default
    private boolean healthy = true;

    public Server toServer() {
        if (host == null || host.isBlank()) {
            throw new ConfigException("Server host cannot be null or empty");
        }
        if (port < 1 || port > 65535) {
            throw new ConfigException("Server port must be between 1 and 65535, got: " + port);
        }
        if (weight < 1 || weight > 10) {
            throw new ConfigException("Server weight must be between 1 and 10, got: " + weight);
        }
        return new Server(host, port, (byte) weight, healthy);
    }
}
