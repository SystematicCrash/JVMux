package com.jvmux.core.http;

public enum Delimiter {
    CR("\r"),
    LF("\n"),
    CRLF("\r\n");

    private final String value;

    Delimiter(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
