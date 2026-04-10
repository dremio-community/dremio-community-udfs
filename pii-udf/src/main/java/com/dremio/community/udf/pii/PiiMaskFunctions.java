/*
 * Dremio PII UDF — Masking Functions
 * MASK_EMAIL, MASK_PHONE, MASK_SSN, MASK_CREDIT_CARD, MASK_IBAN,
 * MASK_IPV4, MASK_NAME, MASK_DOB, MASK_CUSTOM, PII_REDACT
 */
package com.dremio.community.udf.pii;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class PiiMaskFunctions {

    // helper to write a String result into out/buf (returns null-safe)
    // NOTE: inlined in each eval() — no helper method allowed in Janino scope

    // ── MASK_EMAIL ────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "mask_email", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class MaskEmail implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.maskEmail(s, '*');
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── MASK_EMAIL(input, maskChar) ───────────────────────────────────────────
    @FunctionTemplate(name = "mask_email", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class MaskEmailChar implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Param   NullableVarCharHolder maskChar;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            byte[] mc = new byte[maskChar.end - maskChar.start];
            maskChar.buffer.getBytes(maskChar.start, mc);
            char c = mc.length > 0 ? (char) mc[0] : '*';
            String r = com.dremio.community.udf.pii.PiiUtils.maskEmail(s, c);
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── MASK_PHONE ────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "mask_phone", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class MaskPhone implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.maskPhone(s);
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── MASK_SSN ──────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "mask_ssn", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class MaskSsn implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.maskSsn(s);
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── MASK_CREDIT_CARD ──────────────────────────────────────────────────────
    @FunctionTemplate(name = "mask_credit_card", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class MaskCreditCard implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.maskCreditCard(s);
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── MASK_IBAN ─────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "mask_iban", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class MaskIban implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.maskIban(s);
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── MASK_IPV4 ─────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "mask_ipv4", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class MaskIpv4 implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.maskIpv4(s, 2);
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── MASK_NAME ─────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "mask_name", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class MaskName implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.maskName(s);
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── MASK_DOB ──────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "mask_dob", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class MaskDob implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.maskDateOfBirth(s);
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── MASK_CUSTOM(input, showLeft, showRight) ───────────────────────────────
    @FunctionTemplate(name = "mask_custom", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class MaskCustom implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Param   org.apache.arrow.vector.holders.NullableIntHolder showLeft;
        @Param   org.apache.arrow.vector.holders.NullableIntHolder showRight;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            int l = showLeft.isSet == 1 ? showLeft.value : 0;
            int ri = showRight.isSet == 1 ? showRight.value : 0;
            String r = com.dremio.community.udf.pii.PiiUtils.maskCustom(s, l, ri, '*');
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── MASK_CUSTOM(input, showLeft, showRight, maskChar) ─────────────────────
    @FunctionTemplate(name = "mask_custom", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class MaskCustomChar implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Param   org.apache.arrow.vector.holders.NullableIntHolder showLeft;
        @Param   org.apache.arrow.vector.holders.NullableIntHolder showRight;
        @Param   NullableVarCharHolder maskChar;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            int l = showLeft.isSet == 1 ? showLeft.value : 0;
            int ri = showRight.isSet == 1 ? showRight.value : 0;
            byte[] mc = new byte[maskChar.end - maskChar.start];
            maskChar.buffer.getBytes(maskChar.start, mc);
            char c = mc.length > 0 ? (char) mc[0] : '*';
            String r = com.dremio.community.udf.pii.PiiUtils.maskCustom(s, l, ri, c);
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── PII_REDACT ────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "pii_redact", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class PiiRedact implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String r = com.dremio.community.udf.pii.PiiUtils.redact(s);
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }
}
