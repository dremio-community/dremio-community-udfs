package com.dremio.community.udf.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.zip.CRC32;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Static utility methods for crypto/hash UDFs.
 * All methods are stateless and thread-safe.
 * Called from Dremio UDF eval() bodies via fully-qualified class names (Janino requirement).
 */
public final class CryptoUtils {

    private CryptoUtils() {}

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    // ── Internal helpers ──────────────────────────────────────────────────────

    private static String bytesToHex(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[i * 2]     = HEX[v >>> 4];
            out[i * 2 + 1] = HEX[v & 0x0F];
        }
        return new String(out);
    }

    private static byte[] digest(String algorithm, String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    // ── Hashing ───────────────────────────────────────────────────────────────

    public static String md5(String input) throws Exception {
        return bytesToHex(digest("MD5", input));
    }

    public static String sha1(String input) throws Exception {
        return bytesToHex(digest("SHA-1", input));
    }

    public static String sha256(String input) throws Exception {
        return bytesToHex(digest("SHA-256", input));
    }

    public static String sha512(String input) throws Exception {
        return bytesToHex(digest("SHA-512", input));
    }

    public static long crc32(String input) {
        CRC32 crc = new CRC32();
        crc.update(input.getBytes(StandardCharsets.UTF_8));
        return crc.getValue();
    }

    // ── HMAC ─────────────────────────────────────────────────────────────────

    private static String hmac(String algorithm, String message, String key) throws Exception {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm));
        return bytesToHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
    }

    public static String hmacSha256(String message, String key) throws Exception {
        return hmac("HmacSHA256", message, key);
    }

    public static String hmacSha512(String message, String key) throws Exception {
        return hmac("HmacSHA512", message, key);
    }

    // ── Encoding ──────────────────────────────────────────────────────────────

    public static String base64Encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64Decode(String input) throws Exception {
        return new String(Base64.getDecoder().decode(input.trim()), StandardCharsets.UTF_8);
    }

    public static String hexEncode(String input) {
        return bytesToHex(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String hexDecode(String input) throws Exception {
        String s = input.trim();
        if (s.length() % 2 != 0) throw new IllegalArgumentException("Odd-length hex string");
        byte[] bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // ── AES-256-CBC Encryption ────────────────────────────────────────────────
    // Key is hashed to 32 bytes (SHA-256) so any-length key string is accepted.
    // Output format: base64(iv + ciphertext) — IV is prepended for decrypt.

    private static SecretKey deriveKey(String keyStr) throws Exception {
        byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                .digest(keyStr.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String aesEncrypt(String plaintext, String key) throws Exception {
        SecretKey secretKey = deriveKey(key);
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        // Prepend IV to ciphertext, then base64-encode the whole thing
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String aesDecrypt(String encryptedBase64, String key) throws Exception {
        SecretKey secretKey = deriveKey(key);
        byte[] combined = Base64.getDecoder().decode(encryptedBase64.trim());
        if (combined.length < 17) throw new IllegalArgumentException("Invalid ciphertext");
        byte[] iv = new byte[16];
        System.arraycopy(combined, 0, iv, 0, 16);
        byte[] ciphertext = new byte[combined.length - 16];
        System.arraycopy(combined, 16, ciphertext, 0, ciphertext.length);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    public static String randomUuid() {
        return java.util.UUID.randomUUID().toString();
    }

    /** Constant-time string equality to prevent timing attacks. */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return a == b;
        byte[] ba = a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(StandardCharsets.UTF_8);
        int diff = ba.length ^ bb.length;
        int len = Math.min(ba.length, bb.length);
        for (int i = 0; i < len; i++) diff |= ba[i] ^ bb[i];
        return diff == 0;
    }
}
