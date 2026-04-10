/*
 * Dremio PII UDF — Detection Functions
 * IS_EMAIL, IS_PHONE, IS_SSN, IS_CREDIT_CARD, IS_IBAN, IS_IPV4, IS_IPV6,
 * IS_ZIP, IS_DOB, IS_PASSPORT, IS_VIN, IS_NPI, IS_EIN, IS_MAC, IS_URL,
 * IS_PII, PII_TYPE, PII_SCORE
 */
package com.dremio.community.udf.pii;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableVarCharHolder;
import org.apache.arrow.vector.holders.NullableIntHolder;

public class PiiDetectFunctions {

    // ── IS_EMAIL ──────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_email", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsEmail implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isEmail(s) ? 1 : 0;
        }
    }

    // ── IS_PHONE ──────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_phone", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsPhone implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isPhone(s) ? 1 : 0;
        }
    }

    // ── IS_SSN ────────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_ssn", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsSsn implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isSsn(s) ? 1 : 0;
        }
    }

    // ── IS_CREDIT_CARD ────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_credit_card", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsCreditCard implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isCreditCard(s) ? 1 : 0;
        }
    }

    // ── IS_IBAN ───────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_iban", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsIban implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isIban(s) ? 1 : 0;
        }
    }

    // ── IS_IPV4 ───────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_ipv4", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsIpv4 implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isIpv4(s) ? 1 : 0;
        }
    }

    // ── IS_IPV6 ───────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_ipv6", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsIpv6 implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isIpv6(s) ? 1 : 0;
        }
    }

    // ── IS_ZIP ────────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_zip", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsZip implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isUsZip(s) ? 1 : 0;
        }
    }

    // ── IS_DOB ────────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_dob", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsDob implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isDateOfBirth(s) ? 1 : 0;
        }
    }

    // ── IS_PASSPORT ───────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_passport", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsPassport implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isPassport(s) ? 1 : 0;
        }
    }

    // ── IS_VIN ────────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_vin", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsVin implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isVin(s) ? 1 : 0;
        }
    }

    // ── IS_NPI ────────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_npi", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsNpi implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isNpi(s) ? 1 : 0;
        }
    }

    // ── IS_EIN ────────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_ein", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsEin implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isEin(s) ? 1 : 0;
        }
    }

    // ── IS_MAC ────────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_mac", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsMac implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isMacAddress(s) ? 1 : 0;
        }
    }

    // ── IS_URL ────────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_url", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsUrl implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isUrl(s) ? 1 : 0;
        }
    }

    // ── IS_PII ────────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "is_pii", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class IsPii implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.isPii(s) ? 1 : 0;
        }
    }

    // ── PII_TYPE ──────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "pii_type", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class PiiType implements SimpleFunction {
        @Param   NullableVarCharHolder input;
        @Inject  ArrowBuf              buf;
        @Output  NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            String type = com.dremio.community.udf.pii.PiiUtils.piiType(s);
            if (type == null) { out.isSet = 0; return; }
            byte[] bOut = type.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── PII_SCORE ─────────────────────────────────────────────────────────────
    @FunctionTemplate(name = "pii_score", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class PiiScore implements SimpleFunction {
        @Param  NullableVarCharHolder input;
        @Output NullableIntHolder     out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            if (input.isSet == 0) { out.value = 0; return; }
            byte[] b = new byte[input.end - input.start];
            input.buffer.getBytes(input.start, b);
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.value = com.dremio.community.udf.pii.PiiUtils.piiScore(s);
        }
    }
}
