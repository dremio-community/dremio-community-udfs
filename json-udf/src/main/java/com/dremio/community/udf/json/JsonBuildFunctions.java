/*
 * Dremio JSON UDF — Build Functions
 * JSON_FROM_KV, JSON_WRAP_STR
 */
package com.dremio.community.udf.json;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class JsonBuildFunctions {

    // ── JSON_FROM_KV(key, value) → VARCHAR ────────────────────────────────────
    // Creates a single-key JSON object: JSON_FROM_KV('name', 'alice') → {"name":"alice"}
    @FunctionTemplate(name = "json_from_kv", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class JsonFromKV implements SimpleFunction {
        @Param  NullableVarCharHolder key;
        @Param  NullableVarCharHolder value;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] kb = new byte[key.end - key.start];
            key.buffer.getBytes(key.start, kb);
            byte[] vb = new byte[value.end - value.start];
            value.buffer.getBytes(value.start, vb);
            String r = com.dremio.community.udf.json.JsonUtils.fromKV(
                new String(kb, java.nio.charset.StandardCharsets.UTF_8),
                new String(vb, java.nio.charset.StandardCharsets.UTF_8));
            byte[] rb = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(rb.length);
            buf.setBytes(0, rb);
            out.isSet = 1; out.start = 0; out.end = rb.length; out.buffer = buf;
        }
    }

    // ── JSON_WRAP_STR(value) → VARCHAR ────────────────────────────────────────
    // Wraps a plain string as a JSON string literal with proper escaping:
    //   JSON_WRAP_STR('hello "world"') → "hello \"world\""
    @FunctionTemplate(name = "json_wrap_str", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class JsonWrapStr implements SimpleFunction {
        @Param  NullableVarCharHolder value;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] vb = new byte[value.end - value.start];
            value.buffer.getBytes(value.start, vb);
            String r = com.dremio.community.udf.json.JsonUtils.wrapStr(
                new String(vb, java.nio.charset.StandardCharsets.UTF_8));
            byte[] rb = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(rb.length);
            buf.setBytes(0, rb);
            out.isSet = 1; out.start = 0; out.end = rb.length; out.buffer = buf;
        }
    }
}
