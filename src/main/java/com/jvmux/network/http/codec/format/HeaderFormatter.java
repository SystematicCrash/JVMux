package com.jvmux.network.http.codec.format;

import com.jvmux.network.http.constant.Constants;
import com.jvmux.network.http.constant.HttpVersion;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class HeaderFormatter {

    public static Optional<String> formatLocation(String location, String oldDomain, String newDomain) {
        if (location == null || oldDomain == null || newDomain == null)
            return Optional.empty();

        if (location.contains(oldDomain))
            return location.replace(oldDomain, newDomain).describeConstable();

        return location.describeConstable();
    }

    public static String formatForwardedFor(String forwardedFor, String clientIp) {
        return forwardedFor == null ? clientIp : forwardedFor + ", " + clientIp;
    }

    public static String formatDate() {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
    }

    public static String formatKeepAlive() {
        return "timeout=" + Constants.IDLE_TIMEOUT + ", max=" + Constants.MAX_REQUESTS;
    }

    public static String formatVia(String existingVia) {
        String jvmuxVia = HttpVersion.HTTP_1_1.value() + " JVMux";
        return (existingVia == null || existingVia.isBlank())
                ? jvmuxVia
                : existingVia + ", " + jvmuxVia;
    }
}
