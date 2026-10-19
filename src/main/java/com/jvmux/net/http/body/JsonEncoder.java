package com.jvmux.net.http.body;

import java.nio.charset.StandardCharsets;

public class JsonEncoder implements BodyEncoder {

    @Override
    public byte[] encode(Object content) {
        return toJson(content).getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public String contentType() {
        return "application/json";
    }

    private static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "\"" + escapeJson((String) value) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof java.util.Map) {
            return toJsonMap((java.util.Map<?, ?>) value);
        }
        if (value instanceof java.util.Collection) {
            return toJsonCollection((java.util.Collection<?>) value);
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private static String toJsonMap(java.util.Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append(toJson(entry.getKey()));
            sb.append(":");
            sb.append(toJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    private static String toJsonCollection(java.util.Collection<?> collection) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : collection) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append(toJson(item));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
