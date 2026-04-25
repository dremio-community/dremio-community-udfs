/*
 * Dremio Crypto UDF — Utility Functions
 * CRYPTO_RANDOM_UUID(), CRYPTO_CONSTANT_TIME_EQUALS(a, b)
 */
package com.dremio.community.udf.crypto;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableBitHolder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class CryptoUtilityFunctions {

    // ── CRYPTO_RANDOM_UUID() → VARCHAR ────────────────────────────────────────
    @FunctionTemplate(name = "crypto_random_uuid", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class CryptoRandomUuid implements SimpleFunction {
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            String r = com.dremio.community.udf.crypto.CryptoUtils.randomUuid();
            byte[] bOut = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
        }
    }

    // ── CRYPTO_CONSTANT_TIME_EQUALS(a, b) → BIT ───────────────────────────────
    // Constant-time comparison to prevent timing attacks when checking hashes/tokens
    @FunctionTemplate(name = "crypto_constant_time_equals", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class CryptoConstantTimeEquals implements SimpleFunction {
        @Param  NullableVarCharHolder a;
        @Param  NullableVarCharHolder b;
        @Output NullableBitHolder     out;
        public void setup() {}
        public void eval() {
            byte[] bA = new byte[a.end - a.start];
            a.buffer.getBytes(a.start, bA);
            String strA = new String(bA, java.nio.charset.StandardCharsets.UTF_8);
            byte[] bB = new byte[b.end - b.start];
            b.buffer.getBytes(b.start, bB);
            String strB = new String(bB, java.nio.charset.StandardCharsets.UTF_8);
            out.isSet = 1;
            out.value = com.dremio.community.udf.crypto.CryptoUtils.constantTimeEquals(strA, strB) ? 1 : 0;
        }
    }
}
