/*
 * Dremio Crypto UDF — Hash Functions
 * CRYPTO_MD5, CRYPTO_SHA1, CRYPTO_SHA256, CRYPTO_SHA512, CRYPTO_CRC32
 */
package com.dremio.community.udf.crypto;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableBigIntHolder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class CryptoHashFunctions {

    // ── CRYPTO_MD5(input) → VARCHAR ───────────────────────────────────────────
    @FunctionTemplate(name = "crypto_md5", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoMd5 implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try { r = com.dremio.community.udf.crypto.CryptoUtils.md5(s); }
            catch (Exception e) { r = "ERROR:" + e.getMessage(); }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── CRYPTO_SHA1(input) → VARCHAR ─────────────────────────────────────────
    @FunctionTemplate(name = "crypto_sha1", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoSha1 implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try { r = com.dremio.community.udf.crypto.CryptoUtils.sha1(s); }
            catch (Exception e) { r = "ERROR:" + e.getMessage(); }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── CRYPTO_SHA256(input) → VARCHAR ───────────────────────────────────────
    @FunctionTemplate(name = "crypto_sha256", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoSha256 implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try { r = com.dremio.community.udf.crypto.CryptoUtils.sha256(s); }
            catch (Exception e) { r = "ERROR:" + e.getMessage(); }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── CRYPTO_SHA512(input) → VARCHAR ───────────────────────────────────────
    @FunctionTemplate(name = "crypto_sha512", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoSha512 implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try { r = com.dremio.community.udf.crypto.CryptoUtils.sha512(s); }
            catch (Exception e) { r = "ERROR:" + e.getMessage(); }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── CRYPTO_CRC32(input) → BIGINT ─────────────────────────────────────────
    @FunctionTemplate(name = "crypto_crc32", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoCrc32 implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableBigIntHolder  out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.isSet = 1;
            out.value = com.dremio.community.udf.crypto.CryptoUtils.crc32(s);
        }
    }
}
