/*
 * Dremio PII UDF — Hash / Tokenization Functions
 * PII_SHA256, PII_SHA256(input, salt), PII_MD5, PII_TOKENIZE
 */
package com.dremio.community.udf.pii;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class PiiHashFunctions {

    // ── PII_SHA256(input) ─────────────────────────────────────────────────────
    @FunctionTemplate(name = "pii_sha256", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class PiiSha256 implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try {
                r = com.dremio.community.udf.pii.PiiUtils.sha256Hex(s);
            } catch (Exception e) {
                r = "ERROR";
            }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── PII_SHA256(input, salt) ───────────────────────────────────────────────
    @FunctionTemplate(name = "pii_sha256", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class PiiSha256Salted implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Param   NullableVarCharHolder salt;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            byte[] bSalt = new byte[salt.end - salt.start];
            salt.buffer.getBytes(salt.start, bSalt);
            String saltStr = new String(bSalt, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try {
                r = com.dremio.community.udf.pii.PiiUtils.sha256Hex(s, saltStr);
            } catch (Exception e) {
                r = "ERROR";
            }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── PII_MD5 ───────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "pii_md5", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class PiiMd5 implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try {
                r = com.dremio.community.udf.pii.PiiUtils.md5Hex(s);
            } catch (Exception e) {
                r = "ERROR";
            }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── PII_TOKENIZE ──────────────────────────────────────────────────────────
    // Returns "TOK-<first 16 hex chars of SHA-256>" — a stable, reversible-free token
    @FunctionTemplate(name = "pii_tokenize", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class PiiTokenize implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r;
            try {
                r = com.dremio.community.udf.pii.PiiUtils.tokenize(s);
            } catch (Exception e) {
                r = "TOK-ERROR";
            }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }
}
