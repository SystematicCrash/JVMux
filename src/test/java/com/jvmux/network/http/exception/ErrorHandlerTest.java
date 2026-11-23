package com.jvmux.network.http.exception;

import com.jvmux.network.http.constant.HttpHeader;
import com.jvmux.network.http.model.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlerTest {

    @Test
    void shouldConvertHttpExceptionToResponseWithStatusAndMessage() {
        HttpException exception = HttpException.notFound();
        HttpResponse response = ErrorHandler.toResponse(exception);

        assertEquals(404, response.getStatus());
        assertEquals("text/plain", response.getHeader(HttpHeader.CONTENT_TYPE));
    }

    @Test
    void shouldConvertGenericExceptionToDefault500Response() {
        Exception genericException = new IOException("Disk failure");
        HttpResponse response = ErrorHandler.toResponse(genericException);

        assertEquals(500, response.getStatus());
        assertEquals("text/plain", response.getHeader(HttpHeader.CONTENT_TYPE));
    }

    @Test
    void shouldHandleHttpExceptionWithEmptyMessage() {
        HttpException exception = new HttpException(400, "");
        HttpResponse response = ErrorHandler.toResponse(exception);

        assertEquals(400, response.getStatus());
    }
}
