/*
 * Dremio Crypto UDF — HMAC Functions
 * CRYPTO_HMAC_SHA256(message, key), CRYPTO_HMAC_SHA512(message, key)
 */
package com.dremio.community.udf.crypto;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class CryptoHmacFunctions {

    // ── CRYPTO_HMAC_SHA256(message, key) → VARCHAR ────────────────────────────
    @FunctionTemplate(name = "crypto_hmac_sha256", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoHmacSha256 implements SimpleFunction {
        @Param  NullableVarCharHolder message;
        @Param  NullableVarCharHolder key;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] bMsg = new byte[message.end - message.start];
            message.buffer.getBytes(message.start, bMsg);
            String msg = new String(bMsg, java.nio.charset.StandardCharsets.UTF_8);
            byte[] bKey = new byte[key.end - key.start];
            key.buffer.getBytes(key.start, bKey);
            String keyStr = new String(bKey, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try { r = com.dremio.community.udf.crypto.CryptoUtils.hmacSha256(msg, keyStr); }
            catch (Exception e) { r = "ERROR:" + e.getMessage(); }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── CRYPTO_HMAC_SHA512(message, key) → VARCHAR ────────────────────────────
    @FunctionTemplate(name = "crypto_hmac_sha512", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoHmacSha512 implements SimpleFunction {
        @Param  NullableVarCharHolder message;
        @Param  NullableVarCharHolder key;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] bMsg = new byte[message.end - message.start];
            message.buffer.getBytes(message.start, bMsg);
            String msg = new String(bMsg, java.nio.charset.StandardCharsets.UTF_8);
            byte[] bKey = new byte[key.end - key.start];
            key.buffer.getBytes(key.start, bKey);
            String keyStr = new String(bKey, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try { r = com.dremio.community.udf.crypto.CryptoUtils.hmacSha512(msg, keyStr); }
            catch (Exception e) { r = "ERROR:" + e.getMessage(); }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }
}
