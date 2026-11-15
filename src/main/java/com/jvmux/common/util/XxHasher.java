package com.jvmux.common.util;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHash64;
import net.jpountz.xxhash.XXHashFactory;

import java.nio.charset.StandardCharsets;

public final class XxHasher {
    private static final XXHashFactory FACTORY = XXHashFactory.fastestInstance();
    private static final XXHash32 HASH_32 = FACTORY.hash32();
    private static final XXHash64 HASH_64 = FACTORY.hash64();
    private static final int SEED = 0x9747b28c;

    private XxHasher() {}

    public static int hash32(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.US_ASCII);
        return HASH_32.hash(bytes, 0, bytes.length, SEED);
    }

    public static long hash64(byte[] bytes) {
        return HASH_64.hash(bytes, 0, bytes.length, SEED);
    }
}