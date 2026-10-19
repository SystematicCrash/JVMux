package com.jvmux.network.http.codec;

import com.jvmux.network.http.constant.HttpStatus;
import com.jvmux.network.http.constant.HttpVersion;
import com.jvmux.network.http.exception.HttpException;
import com.jvmux.network.http.model.HttpResponse;

import java.io.InputStream;
import java.util.Arrays;

import static com.jvmux.network.http.constant.Constants.*;

public class ResponseParser {

    public static HttpResponse parse(String[] raw) {
        return parse(raw, null);
    }

    public static HttpResponse parse(String[] raw, InputStream body) {
        if (raw.length == 0) {
            throw HttpException.badGateway();
        }
        var parts = parseResponseLine(raw[0]);

        return HttpResponse.builder()
                .version(HttpVersion.valueOf(parts[0]))
                .status(Integer.parseInt(parts[1]))
                .headers(HeaderParser.parseHeaders(
                        Arrays.copyOfRange(raw, 1, raw.length)
                ))
                .body(body)
                .build();
    }

    private static String[] parseResponseLine(String line) {
        if (line.isEmpty()) {
            throw HttpException.badGateway();
        }
        var parts = line.split(String.valueOf(SP));

        if (parts.length != 3 && parts.length != 2) {
            throw HttpException.badGateway();
        }

        if (!VALID_VERSIONS.contains(parts[0])) {
            throw HttpException.badGateway();
        }
        if (!HttpStatus.isValid(Integer.parseInt(parts[1]))) {
            throw HttpException.badGateway();
        }

        return parts;
    }
}
