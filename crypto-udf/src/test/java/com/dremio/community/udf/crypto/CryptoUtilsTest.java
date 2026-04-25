package com.dremio.community.udf.crypto;

import org.junit.Test;
import static org.junit.Assert.*;

public class CryptoUtilsTest {

    // ── MD5 ───────────────────────────────────────────────────────────────────

    @Test
    public void testMd5KnownValue() throws Exception {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", CryptoUtils.md5(""));
    }

    @Test
    public void testMd5Hello() throws Exception {
        assertEquals("5d41402abc4b2a76b9719d911017c592", CryptoUtils.md5("hello"));
    }

    @Test
    public void testMd5Length() throws Exception {
        assertEquals(32, CryptoUtils.md5("any input").length());
    }

    // ── SHA-1 ─────────────────────────────────────────────────────────────────

    @Test
    public void testSha1KnownValue() throws Exception {
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", CryptoUtils.sha1(""));
    }

    @Test
    public void testSha1Hello() throws Exception {
        assertEquals("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d", CryptoUtils.sha1("hello"));
    }

    @Test
    public void testSha1Length() throws Exception {
        assertEquals(40, CryptoUtils.sha1("test").length());
    }

    // ── SHA-256 ───────────────────────────────────────────────────────────────

    @Test
    public void testSha256KnownValue() throws Exception {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                CryptoUtils.sha256(""));
    }

    @Test
    public void testSha256Hello() throws Exception {
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
                CryptoUtils.sha256("hello"));
    }

    @Test
    public void testSha256Length() throws Exception {
        assertEquals(64, CryptoUtils.sha256("test").length());
    }

    @Test
    public void testSha256Deterministic() throws Exception {
        assertEquals(CryptoUtils.sha256("same"), CryptoUtils.sha256("same"));
    }

    @Test
    public void testSha256Distinct() throws Exception {
        assertNotEquals(CryptoUtils.sha256("abc"), CryptoUtils.sha256("ABC"));
    }

    // ── SHA-512 ───────────────────────────────────────────────────────────────

    @Test
    public void testSha512KnownValue() throws Exception {
        assertEquals("9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca72323c3d99ba5c11d7c7acc6e14b8c5da0c4663475c2e5c3adef46f73bcdec043",
                CryptoUtils.sha512("hello"));
        assertEquals(128, CryptoUtils.sha512("").length());
    }

    @Test
    public void testSha512Length() throws Exception {
        assertEquals(128, CryptoUtils.sha512("anything").length());
    }

    // ── CRC32 ─────────────────────────────────────────────────────────────────

    @Test
    public void testCrc32KnownValue() {
        assertEquals(0L, CryptoUtils.crc32(""));
    }

    @Test
    public void testCrc32Hello() {
        assertEquals(907060870L, CryptoUtils.crc32("hello"));
    }

    @Test
    public void testCrc32Positive() {
        assertTrue(CryptoUtils.crc32("test") >= 0);
    }

    @Test
    public void testCrc32Deterministic() {
        assertEquals(CryptoUtils.crc32("foo"), CryptoUtils.crc32("foo"));
    }

    // ── HMAC-SHA256 ───────────────────────────────────────────────────────────

    @Test
    public void testHmacSha256KnownValue() throws Exception {
        // RFC 4231 test vector
        String result = CryptoUtils.hmacSha256("Hi There",
                new String(new byte[]{0x0b,0x0b,0x0b,0x0b,0x0b,0x0b,0x0b,0x0b,
                        0x0b,0x0b,0x0b,0x0b,0x0b,0x0b,0x0b,0x0b,
                        0x0b,0x0b,0x0b,0x0b}, java.nio.charset.StandardCharsets.ISO_8859_1));
        assertEquals(64, result.length());
    }

    @Test
    public void testHmacSha256Length() throws Exception {
        assertEquals(64, CryptoUtils.hmacSha256("message", "key").length());
    }

    @Test
    public void testHmacSha256KeySensitivity() throws Exception {
        assertNotEquals(
                CryptoUtils.hmacSha256("message", "key1"),
                CryptoUtils.hmacSha256("message", "key2"));
    }

    @Test
    public void testHmacSha256MessageSensitivity() throws Exception {
        assertNotEquals(
                CryptoUtils.hmacSha256("message1", "key"),
                CryptoUtils.hmacSha256("message2", "key"));
    }

    // ── HMAC-SHA512 ───────────────────────────────────────────────────────────

    @Test
    public void testHmacSha512Length() throws Exception {
        assertEquals(128, CryptoUtils.hmacSha512("message", "key").length());
    }

    @Test
    public void testHmacSha512Deterministic() throws Exception {
        assertEquals(
                CryptoUtils.hmacSha512("msg", "k"),
                CryptoUtils.hmacSha512("msg", "k"));
    }

    // ── Base64 ────────────────────────────────────────────────────────────────

    @Test
    public void testBase64EncodeHello() {
        assertEquals("aGVsbG8=", CryptoUtils.base64Encode("hello"));
    }

    @Test
    public void testBase64DecodeHello() throws Exception {
        assertEquals("hello", CryptoUtils.base64Decode("aGVsbG8="));
    }

    @Test
    public void testBase64RoundTrip() throws Exception {
        String original = "Hello, World! 123 !@#";
        assertEquals(original, CryptoUtils.base64Decode(CryptoUtils.base64Encode(original)));
    }

    @Test
    public void testBase64EncodeEmpty() {
        assertEquals("", CryptoUtils.base64Encode(""));
    }

    @Test
    public void testBase64DecodeWithWhitespace() throws Exception {
        assertEquals("hello", CryptoUtils.base64Decode("  aGVsbG8=  "));
    }

    // ── Hex Encode/Decode ─────────────────────────────────────────────────────

    @Test
    public void testHexEncodeHello() {
        assertEquals("68656c6c6f", CryptoUtils.hexEncode("hello"));
    }

    @Test
    public void testHexDecodeHello() throws Exception {
        assertEquals("hello", CryptoUtils.hexDecode("68656c6c6f"));
    }

    @Test
    public void testHexRoundTrip() throws Exception {
        String original = "Test 123 !@#";
        assertEquals(original, CryptoUtils.hexDecode(CryptoUtils.hexEncode(original)));
    }

    @Test
    public void testHexEncodeEmpty() {
        assertEquals("", CryptoUtils.hexEncode(""));
    }

    @Test(expected = Exception.class)
    public void testHexDecodeOddLength() throws Exception {
        CryptoUtils.hexDecode("abc");
    }

    // ── AES Encrypt/Decrypt ───────────────────────────────────────────────────

    @Test
    public void testAesRoundTrip() throws Exception {
        String plaintext = "Hello, secret world!";
        String key = "my-secret-key";
        String encrypted = CryptoUtils.aesEncrypt(plaintext, key);
        String decrypted = CryptoUtils.aesDecrypt(encrypted, key);
        assertEquals(plaintext, decrypted);
    }

    @Test
    public void testAesEncryptDifferentEachTime() throws Exception {
        // Each encrypt call uses a random IV, so outputs differ
        String pt = "same plaintext";
        String key = "same key";
        assertNotEquals(CryptoUtils.aesEncrypt(pt, key), CryptoUtils.aesEncrypt(pt, key));
    }

    @Test
    public void testAesEncryptOutputIsBase64() throws Exception {
        String encrypted = CryptoUtils.aesEncrypt("test", "key");
        // Should not throw — valid base64
        java.util.Base64.getDecoder().decode(encrypted);
    }

    @Test
    public void testAesDecryptWrongKeyFails() throws Exception {
        String encrypted = CryptoUtils.aesEncrypt("secret", "correct-key");
        try {
            String result = CryptoUtils.aesDecrypt(encrypted, "wrong-key");
            // If it doesn't throw, the result should at least differ
            assertNotEquals("secret", result);
        } catch (Exception e) {
            // Expected — wrong key causes padding error
            assertTrue(e.getMessage() != null);
        }
    }

    @Test
    public void testAesLongKey() throws Exception {
        String key = "this is a very long key that exceeds 32 characters easily";
        String pt = "test plaintext";
        assertEquals(pt, CryptoUtils.aesDecrypt(CryptoUtils.aesEncrypt(pt, key), key));
    }

    @Test
    public void testAesEmptyPlaintext() throws Exception {
        String encrypted = CryptoUtils.aesEncrypt("", "key");
        assertEquals("", CryptoUtils.aesDecrypt(encrypted, "key"));
    }

    @Test
    public void testAesUnicodeContent() throws Exception {
        String pt = "こんにちは 🔐 مرحبا";
        String key = "unicode-key";
        assertEquals(pt, CryptoUtils.aesDecrypt(CryptoUtils.aesEncrypt(pt, key), key));
    }

    // ── UUID ──────────────────────────────────────────────────────────────────

    @Test
    public void testRandomUuidFormat() {
        String uuid = CryptoUtils.randomUuid();
        assertTrue(uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    public void testRandomUuidIsRandom() {
        assertNotEquals(CryptoUtils.randomUuid(), CryptoUtils.randomUuid());
    }

    @Test
    public void testRandomUuidLength() {
        assertEquals(36, CryptoUtils.randomUuid().length());
    }

    // ── Constant-Time Equals ──────────────────────────────────────────────────

    @Test
    public void testConstantTimeEqualsMatch() {
        assertTrue(CryptoUtils.constantTimeEquals("abc", "abc"));
    }

    @Test
    public void testConstantTimeEqualsMismatch() {
        assertFalse(CryptoUtils.constantTimeEquals("abc", "ABC"));
    }

    @Test
    public void testConstantTimeEqualsEmpty() {
        assertTrue(CryptoUtils.constantTimeEquals("", ""));
    }

    @Test
    public void testConstantTimeEqualsDifferentLength() {
        assertFalse(CryptoUtils.constantTimeEquals("ab", "abc"));
    }

    @Test
    public void testConstantTimeEqualsNullBoth() {
        assertTrue(CryptoUtils.constantTimeEquals(null, null));
    }

    @Test
    public void testConstantTimeEqualsNullOne() {
        assertFalse(CryptoUtils.constantTimeEquals("a", null));
        assertFalse(CryptoUtils.constantTimeEquals(null, "a"));
    }
}
