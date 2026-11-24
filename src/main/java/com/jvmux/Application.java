package com.jvmux;

import com.jvmux.config.ConfigLoader;
import com.jvmux.config.JvmuxConfig;
import com.jvmux.config.ListenerConfig;
import com.jvmux.config.YamlConfigLoader;
import com.jvmux.network.proxy.ProxyRelay;
import com.jvmux.network.routing.RouteTree;
import com.jvmux.network.routing.Router;
import com.jvmux.network.tcp.TCPServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application implements AutoCloseable {

    private final JvmuxConfig config;
    private final RouteTree routeTree;
    private TCPServer tcpServer;
    private ExecutorService executor;
    private volatile boolean running = false;

    public Application(JvmuxConfig config) throws Exception {
        this.config = config;
        this.routeTree = Router.fromConfig(config).build();
    }

    public static void bootstrap() throws Exception {
        bootstrap(null);
    }

    public static void bootstrap(String configPath) throws Exception {
        JvmuxConfig config = loadConfiguration(configPath);
        Application app = new Application(config);
        app.start();
    }

    public static JvmuxConfig loadConfiguration(String configPath) {
        ConfigLoader loader = new YamlConfigLoader();

        if (configPath != null && !configPath.isBlank()) {
            return loader.load(Path.of(configPath));
        }

        String sysProp = System.getProperty("jvmux.config");
        if (sysProp != null && !sysProp.isBlank()) {
            return loader.load(Path.of(sysProp));
        }

        String envVar = System.getenv("JVMUX_CONFIG");
        if (envVar != null && !envVar.isBlank()) {
            return loader.load(Path.of(envVar));
        }

        Path localFile = Path.of("jvmux.yaml");
        if (Files.exists(localFile)) {
            return loader.load(localFile);
        }
        return ((YamlConfigLoader) loader).loadFromClasspath("jvmux.yaml");
    }

    public void start() throws Exception {
        ListenerConfig listener = config.getListener();
        this.tcpServer = new TCPServer(listener.getHost(), listener.getPort(), listener.getBacklog());
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.running = true;

        System.out.printf("JVMux is up and running on %s:%d%n", listener.getHost(), listener.getPort());

        while (running && !tcpServer.isClosed()) {
            try {
                var socket = tcpServer.accept();
                executor.submit(() -> {
                    try (var proxy = new ProxyRelay(socket, routeTree)) {
                        proxy.run();
                    } catch (Exception ignored) {
                    }
                });
            } catch (Exception e) {
                if (!running || tcpServer.isClosed()) {
                    break;
                }
            }
        }
    }

    public int getPort() {
        return tcpServer != null ? tcpServer.getPort() : -1;
    }

    public JvmuxConfig getConfig() {
        return config;
    }

    public RouteTree getRouteTree() {
        return routeTree;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void close() throws Exception {
        this.running = false;
        if (tcpServer != null && !tcpServer.isClosed()) {
            tcpServer.close();
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
