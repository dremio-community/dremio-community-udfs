/*
 * Dremio Crypto UDF — Encoding Functions
 * CRYPTO_BASE64_ENCODE, CRYPTO_BASE64_DECODE, CRYPTO_HEX_ENCODE, CRYPTO_HEX_DECODE
 */
package com.dremio.community.udf.crypto;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class CryptoEncodingFunctions {

    // ── CRYPTO_BASE64_ENCODE(input) → VARCHAR ─────────────────────────────────
    @FunctionTemplate(name = "crypto_base64_encode", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoBase64Encode implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.crypto.CryptoUtils.base64Encode(s);
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── CRYPTO_BASE64_DECODE(input) → VARCHAR ─────────────────────────────────
    @FunctionTemplate(name = "crypto_base64_decode", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoBase64Decode implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try { r = com.dremio.community.udf.crypto.CryptoUtils.base64Decode(s); }
            catch (Exception e) { r = "ERROR:" + e.getMessage(); }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── CRYPTO_HEX_ENCODE(input) → VARCHAR ───────────────────────────────────
    @FunctionTemplate(name = "crypto_hex_encode", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoHexEncode implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.crypto.CryptoUtils.hexEncode(s);
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── CRYPTO_HEX_DECODE(input) → VARCHAR ───────────────────────────────────
    @FunctionTemplate(name = "crypto_hex_decode", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoHexDecode implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try { r = com.dremio.community.udf.crypto.CryptoUtils.hexDecode(s); }
            catch (Exception e) { r = "ERROR:" + e.getMessage(); }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }
}
