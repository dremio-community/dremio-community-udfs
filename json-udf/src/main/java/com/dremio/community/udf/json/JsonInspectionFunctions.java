/*
 * Dremio JSON UDF — Inspection Functions
 * JSON_IS_VALID, JSON_TYPE, JSON_LENGTH, JSON_HAS_KEY, JSON_KEYS
 */
package com.dremio.community.udf.json;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableBigIntHolder;
import org.apache.arrow.vector.holders.NullableBitHolder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class JsonInspectionFunctions {

    // ── JSON_IS_VALID(json) → BIT ─────────────────────────────────────────────
    @FunctionTemplate(name = "json_is_valid", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonIsValid implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Output NullableBitHolder     out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0) { out.isSet = 1; out.value = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            String s = new String(jb, java.nio.charset.StandardCharsets.UTF_8);
            out.isSet = 1;
            out.value = com.dremio.community.udf.json.JsonUtils.isValid(s) ? 1 : 0;
        }
    }

    // ── JSON_TYPE(json) → VARCHAR ─────────────────────────────────────────────
    // Returns: "object", "array", "string", "number", "boolean", "null", "invalid"
    @FunctionTemplate(name = "json_type", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonType implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0) { out.isSet = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            String r = com.dremio.community.udf.json.JsonUtils.type(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8));
            byte[] rb = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(rb.length);
            buf.setBytes(0, rb);
            out.isSet = 1; out.start = 0; out.end = rb.length; out.buffer = buf;
        }
    }

    // ── JSON_LENGTH(json) → BIGINT ────────────────────────────────────────────
    // Object: number of keys; Array: number of elements; other: -1
    @FunctionTemplate(name = "json_length", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonLength implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Output NullableBigIntHolder  out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0) { out.isSet = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            out.isSet = 1;
            out.value = com.dremio.community.udf.json.JsonUtils.length(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    // ── JSON_HAS_KEY(json, key) → BIT ─────────────────────────────────────────
    @FunctionTemplate(name = "json_has_key", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonHasKey implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Param  NullableVarCharHolder key;
        @Output NullableBitHolder     out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0 || key.isSet == 0) { out.isSet = 1; out.value = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            byte[] kb = new byte[key.end - key.start];
            key.buffer.getBytes(key.start, kb);
            out.isSet = 1;
            out.value = com.dremio.community.udf.json.JsonUtils.hasKey(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8),
                new String(kb, java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    // ── JSON_KEYS(json) → VARCHAR ─────────────────────────────────────────────
    // Returns comma-separated list of top-level keys, or NULL for non-objects
    @FunctionTemplate(name = "json_keys", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonKeys implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0) { out.isSet = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            String r = com.dremio.community.udf.json.JsonUtils.keys(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8));
            if (r == null) { out.isSet = 0; return; }
            byte[] rb = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(rb.length);
            buf.setBytes(0, rb);
            out.isSet = 1; out.start = 0; out.end = rb.length; out.buffer = buf;
        }
    }
}
