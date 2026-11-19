package com.jvmux.network.http.body;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BodyWriterTest {

    @Test
    void shouldWriteFixedBodyProperly() throws IOException {
        String data = "Hello, JVMux Fixed Body!";
        byte[] bytes = data.getBytes(StandardCharsets.US_ASCII);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        FixedBodyWriter writer = new FixedBodyWriter(in, bytes.length);
        writer.write(out);

        assertArrayEquals(bytes, out.toByteArray());
    }

    @Test
    void shouldThrowOnPrematureEofInFixedBody() {
        byte[] bytes = "short".getBytes(StandardCharsets.US_ASCII);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        FixedBodyWriter writer = new FixedBodyWriter(in, 100); // Expect 100 bytes, only 5 present
        assertThrows(IOException.class, () -> writer.write(out));
    }

    @Test
    void shouldWriteChunkedBodyProperly() throws IOException {
        // Chunk 1: "5\r\nhello\r\n"
        // Chunk 2: "6\r\n world\r\n"
        // Final: "0\r\n\r\n"
        String rawChunked = "5\r\nhello\r\n6\r\n world\r\n0\r\n\r\n";
        byte[] bytes = rawChunked.getBytes(StandardCharsets.US_ASCII);

        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ChunkedBodyWriter writer = new ChunkedBodyWriter(in);
        writer.write(out);

        assertEquals(rawChunked, out.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void shouldWriteChunkedBodyWithChunkExtensionsAndTrailers() throws IOException {
        String rawChunked = "5;ext=foo\r\nhello\r\n0\r\nTrailer-Header: value\r\n\r\n";
        byte[] bytes = rawChunked.getBytes(StandardCharsets.US_ASCII);

        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ChunkedBodyWriter writer = new ChunkedBodyWriter(in);
        writer.write(out);

        assertEquals(rawChunked, out.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void shouldCreateAppropriateWriterFromFactory() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);

        // Null headers
        assertTrue(BodyWriterFactory.create(in, null).isEmpty());

        // Empty headers
        assertTrue(BodyWriterFactory.create(in, new HashMap<>()).isEmpty());

        // Chunked
        Map<String, String> chunkedHeaders = Map.of("transfer-encoding", "chunked");
        Optional<BodyWriter> chunkedWriter = BodyWriterFactory.create(in, chunkedHeaders);
        assertTrue(chunkedWriter.isPresent());
        assertInstanceOf(ChunkedBodyWriter.class, chunkedWriter.get());

        // Fixed length
        Map<String, String> fixedHeaders = Map.of("content-length", "128");
        Optional<BodyWriter> fixedWriter = BodyWriterFactory.create(in, fixedHeaders);
        assertTrue(fixedWriter.isPresent());
        assertInstanceOf(FixedBodyWriter.class, fixedWriter.get());
    }
}
