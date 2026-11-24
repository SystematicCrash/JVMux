package com.jvmux;

public class Main {

    public static void main(String[] args) throws Exception {
        String configPath = (args != null && args.length > 0) ? args[0] : null;
        Application.bootstrap(configPath);
    }
}
