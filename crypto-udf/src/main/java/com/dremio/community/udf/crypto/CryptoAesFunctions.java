/*
 * Dremio Crypto UDF — AES Encryption Functions
 * CRYPTO_AES_ENCRYPT(plaintext, key), CRYPTO_AES_DECRYPT(ciphertext, key)
 *
 * Uses AES-256-CBC. The key string is hashed to 32 bytes via SHA-256 so any
 * length key is accepted. Output is base64(iv + ciphertext) — the IV is
 * randomly generated per call and prepended so CRYPTO_AES_DECRYPT can recover it.
 */
package com.dremio.community.udf.crypto;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class CryptoAesFunctions {

    // ── CRYPTO_AES_ENCRYPT(plaintext, key) → VARCHAR ──────────────────────────
    @FunctionTemplate(name = "crypto_aes_encrypt", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoAesEncrypt implements SimpleFunction {
        @Param  NullableVarCharHolder plaintext;
        @Param  NullableVarCharHolder key;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] bPt = new byte[plaintext.end - plaintext.start];
            plaintext.buffer.getBytes(plaintext.start, bPt);
            String pt = new String(bPt, java.nio.charset.StandardCharsets.UTF_8);
            byte[] bKey = new byte[key.end - key.start];
            key.buffer.getBytes(key.start, bKey);
            String keyStr = new String(bKey, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try { r = com.dremio.community.udf.crypto.CryptoUtils.aesEncrypt(pt, keyStr); }
            catch (Exception e) { r = "ERROR:" + e.getMessage(); }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── CRYPTO_AES_DECRYPT(ciphertext, key) → VARCHAR ─────────────────────────
    @FunctionTemplate(name = "crypto_aes_decrypt", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoAesDecrypt implements SimpleFunction {
        @Param  NullableVarCharHolder ciphertext;
        @Param  NullableVarCharHolder key;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] bCt = new byte[ciphertext.end - ciphertext.start];
            ciphertext.buffer.getBytes(ciphertext.start, bCt);
            String ct = new String(bCt, java.nio.charset.StandardCharsets.UTF_8);
            byte[] bKey = new byte[key.end - key.start];
            key.buffer.getBytes(key.start, bKey);
            String keyStr = new String(bKey, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try { r = com.dremio.community.udf.crypto.CryptoUtils.aesDecrypt(ct, keyStr); }
            catch (Exception e) { r = "ERROR:" + e.getMessage(); }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }
}
