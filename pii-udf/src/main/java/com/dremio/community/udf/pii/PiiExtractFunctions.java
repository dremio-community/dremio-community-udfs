/*
 * Dremio PII UDF — Extraction Functions
 * EXTRACT_EMAIL, EXTRACT_PHONE, EXTRACT_SSN, EXTRACT_CREDIT_CARD,
 * EXTRACT_IBAN, EXTRACT_IPV4, EXTRACT_IPV6, EXTRACT_URL, EXTRACT_ALL_PII
 */
package com.dremio.community.udf.pii;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class PiiExtractFunctions {

    // ── EXTRACT_EMAIL ─────────────────────────────────────────────────────────
    @FunctionTemplate(name = "extract_email", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class ExtractEmail implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.extractEmail(s);
            if (r == null) { out.isSet = 0; return; }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── EXTRACT_PHONE ─────────────────────────────────────────────────────────
    @FunctionTemplate(name = "extract_phone", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class ExtractPhone implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.extractPhone(s);
            if (r == null) { out.isSet = 0; return; }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── EXTRACT_SSN ───────────────────────────────────────────────────────────
    @FunctionTemplate(name = "extract_ssn", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class ExtractSsn implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.extractSsn(s);
            if (r == null) { out.isSet = 0; return; }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── EXTRACT_CREDIT_CARD ───────────────────────────────────────────────────
    @FunctionTemplate(name = "extract_credit_card", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class ExtractCreditCard implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.extractCreditCard(s);
            if (r == null) { out.isSet = 0; return; }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── EXTRACT_IBAN ──────────────────────────────────────────────────────────
    @FunctionTemplate(name = "extract_iban", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class ExtractIban implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.extractIban(s);
            if (r == null) { out.isSet = 0; return; }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── EXTRACT_IPV4 ──────────────────────────────────────────────────────────
    @FunctionTemplate(name = "extract_ipv4", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class ExtractIpv4 implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.extractIpv4(s);
            if (r == null) { out.isSet = 0; return; }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── EXTRACT_IPV6 ──────────────────────────────────────────────────────────
    @FunctionTemplate(name = "extract_ipv6", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class ExtractIpv6 implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.extractIpv6(s);
            if (r == null) { out.isSet = 0; return; }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── EXTRACT_URL ───────────────────────────────────────────────────────────
    @FunctionTemplate(name = "extract_url", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class ExtractUrl implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.extractUrl(s);
            if (r == null) { out.isSet = 0; return; }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── EXTRACT_ALL_PII ───────────────────────────────────────────────────────
    // Returns JSON array of detected PII values e.g. ["user@example.com","555-123-4567"]
    @FunctionTemplate(name = "extract_all_pii", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class ExtractAllPii implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.extractAllPii(s);
            if (r == null) { out.isSet = 0; return; }
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }
}
