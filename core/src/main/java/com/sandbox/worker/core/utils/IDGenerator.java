package com.sandbox.worker.core.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

public class IDGenerator {

    public static final int DEFAULT_BASE62_ID_LENGTH = 22;
    private static final int BYTES_IN_ID = 16;
    private static final ThreadLocal<SecureRandom> threadLocalSecureRandom = new ThreadLocal<>();
    private static BaseConverter base62Converter = new BaseConverter("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");

    private IDGenerator() {

    }

    public static byte[] generateBytes() {
        byte[] idBytes = new byte[BYTES_IN_ID];
        getSecureRandom().nextBytes(idBytes);
        return idBytes;
    }

    public static String generateId() {
        return generateIdWithPrefix(null);
    }

    public static String generateIdWithPrefix(String prefix) {
        if(prefix == null){
            return UUID.randomUUID().toString();
        } else {
            return prefix + "-" + UUID.randomUUID().toString();
        }

    }

    public static String generateRequestId() {
        return generateIdWithPrefix("r");
    }

    public static byte[] decodeId(String idWithoutPrefix) {
        return base62Converter.decode(idWithoutPrefix, BYTES_IN_ID);
    }

    public static String encodeId(byte[] idBytes) {
        return base62Converter.encode(idBytes, DEFAULT_BASE62_ID_LENGTH);
    }

    private static SecureRandom getSecureRandom() {
        SecureRandom secureRandom = threadLocalSecureRandom.get();
        if(secureRandom == null){
            try {
                secureRandom = SecureRandom.getInstance("SHA1PRNG");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            threadLocalSecureRandom.set(secureRandom);
        }
        return secureRandom;
    }
}
