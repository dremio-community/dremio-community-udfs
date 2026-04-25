/*
 * Dremio JSON UDF — Array Functions
 * JSON_ARRAY_LENGTH, JSON_ARRAY_GET, JSON_ARRAY_CONTAINS_STR, JSON_ARRAY_APPEND
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
import org.apache.arrow.vector.holders.NullableIntHolder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class JsonArrayFunctions {

    // ── JSON_ARRAY_LENGTH(json) → BIGINT ──────────────────────────────────────
    // Returns element count for arrays, -1 for non-arrays
    @FunctionTemplate(name = "json_array_length", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonArrayLength implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Output NullableBigIntHolder  out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0) { out.isSet = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            out.isSet = 1;
            out.value = com.dremio.community.udf.json.JsonUtils.arrayLength(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    // ── JSON_ARRAY_GET(json, index) → VARCHAR ────────────────────────────────
    // Returns element at 0-based index as string; NULL if out-of-bounds or not an array
    @FunctionTemplate(name = "json_array_get", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonArrayGet implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Param  NullableIntHolder     index;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0 || index.isSet == 0) { out.isSet = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            String r = com.dremio.community.udf.json.JsonUtils.arrayGet(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8),
                index.value);
            if (r == null) { out.isSet = 0; return; }
            byte[] rb = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(rb.length);
            buf.setBytes(0, rb);
            out.isSet = 1; out.start = 0; out.end = rb.length; out.buffer = buf;
        }
    }

    // ── JSON_ARRAY_CONTAINS_STR(json, value) → BIT ───────────────────────────
    // Returns 1 if any element string-equals value, 0 otherwise
    @FunctionTemplate(name = "json_array_contains_str", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonArrayContainsStr implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Param  NullableVarCharHolder value;
        @Output NullableBitHolder     out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0 || value.isSet == 0) { out.isSet = 1; out.value = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            byte[] vb = new byte[value.end - value.start];
            value.buffer.getBytes(value.start, vb);
            out.isSet = 1;
            out.value = com.dremio.community.udf.json.JsonUtils.arrayContainsStr(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8),
                new String(vb, java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    // ── JSON_ARRAY_APPEND(json, value) → VARCHAR ─────────────────────────────
    // Appends value to array; value parsed as JSON if valid, else as string
    @FunctionTemplate(name = "json_array_append", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class JsonArrayAppend implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Param  NullableVarCharHolder value;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            byte[] vb = new byte[value.end - value.start];
            value.buffer.getBytes(value.start, vb);
            String r = com.dremio.community.udf.json.JsonUtils.arrayAppend(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8),
                new String(vb, java.nio.charset.StandardCharsets.UTF_8));
            byte[] rb = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(rb.length);
            buf.setBytes(0, rb);
            out.isSet = 1; out.start = 0; out.end = rb.length; out.buffer = buf;
        }
    }
}
