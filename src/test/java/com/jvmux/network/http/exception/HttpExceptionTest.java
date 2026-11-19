package com.jvmux.network.http.exception;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class HttpExceptionTest {

    @Test
    void shouldCreateInternalErrorProperly() {
        HttpException ex = HttpException.internalError();
        assertEquals(500, ex.getStatus());
        assertEquals("Internal Server Error", ex.getMessage());

        IOException cause = new IOException("Disk failure");
        HttpException withCause = HttpException.internalError(cause);
        assertEquals(500, withCause.getStatus());
        assertEquals(cause, withCause.getCause());

        HttpException custom = HttpException.internalError("Custom internal", cause);
        assertEquals("Custom internal", custom.getMessage());
        assertEquals(cause, custom.getCause());
    }

    @Test
    void shouldCreateBadRequestProperly() {
        HttpException ex = HttpException.badRequest("Missing param");
        assertEquals(400, ex.getStatus());
        assertEquals("Missing param", ex.getMessage());

        Throwable cause = new IllegalArgumentException("invalid");
        HttpException withCause = HttpException.badRequest("Invalid input", cause);
        assertEquals(400, withCause.getStatus());
        assertEquals(cause, withCause.getCause());
    }

    @Test
    void shouldCreateNotFoundProperly() {
        HttpException ex = HttpException.notFound();
        assertEquals(404, ex.getStatus());
        assertEquals("Requested url not found", ex.getMessage());

        Throwable cause = new RuntimeException("not found");
        HttpException withCause = HttpException.notFound(cause);
        assertEquals(404, withCause.getStatus());
        assertEquals(cause, withCause.getCause());
    }

    @Test
    void shouldCreateMethodNotAllowedProperly() {
        HttpException ex = HttpException.methodNotAllowed();
        assertEquals(405, ex.getStatus());
        assertEquals("Method not allowed for this url", ex.getMessage());

        Throwable cause = new RuntimeException("blocked");
        HttpException withCause = HttpException.methodNotAllowed(cause);
        assertEquals(405, withCause.getStatus());
        assertEquals(cause, withCause.getCause());
    }

    @Test
    void shouldCreateHeadersTooLargeProperly() {
        HttpException ex = HttpException.headersToLarge();
        assertEquals(431, ex.getStatus());
        assertEquals("Request Header Fields Too Large", ex.getMessage());

        Throwable cause = new RuntimeException("too big");
        HttpException withCause = HttpException.headersToLarge(cause);
        assertEquals(431, withCause.getStatus());
        assertEquals(cause, withCause.getCause());
    }

    @Test
    void shouldCreateBadGatewayProperly() {
        HttpException ex = HttpException.badGateway();
        assertEquals(502, ex.getStatus());
        assertEquals("Bad Gateway Error", ex.getMessage());

        HttpException msg = HttpException.badGateway("Custom Bad Gateway");
        assertEquals(502, msg.getStatus());
        assertEquals("Custom Bad Gateway", msg.getMessage());

        Throwable cause = new IOException("upstream dead");
        HttpException withCause = HttpException.badGateway(cause);
        assertEquals(502, withCause.getStatus());
        assertEquals(cause, withCause.getCause());

        HttpException full = HttpException.badGateway("Failed connect", cause);
        assertEquals(502, full.getStatus());
        assertEquals("Failed connect", full.getMessage());
        assertEquals(cause, full.getCause());
    }

    @Test
    void shouldCreateGatewayTimeoutProperly() {
        HttpException ex = HttpException.gatewayTimeout();
        assertEquals(504, ex.getStatus());
        assertEquals("Gateway Timeout Error", ex.getMessage());

        Throwable cause = new IOException("timeout");
        HttpException withCause = HttpException.gatewayTimeout(cause);
        assertEquals(504, withCause.getStatus());
        assertEquals(cause, withCause.getCause());

        HttpException full = HttpException.gatewayTimeout("Upstream timed out", cause);
        assertEquals(504, full.getStatus());
        assertEquals("Upstream timed out", full.getMessage());
        assertEquals(cause, full.getCause());
    }

    @Test
    void shouldReturnSelfOnFillInStackTrace() {
        HttpException ex = new HttpException(500, "Error");
        assertSame(ex, ex.fillInStackTrace());
    }
}
