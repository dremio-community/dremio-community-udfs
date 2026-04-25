/*
 * Dremio JSON UDF — Manipulation Functions
 * JSON_SET, JSON_DELETE, JSON_MERGE, JSON_PRETTY, JSON_MINIFY
 */
package com.dremio.community.udf.json;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class JsonManipulationFunctions {

    // ── JSON_SET(json, key, value) → VARCHAR ─────────────────────────────────
    // Sets top-level key to value. value is parsed as JSON if valid, else stored as string.
    @FunctionTemplate(name = "json_set", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonSet implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Param  NullableVarCharHolder key;
        @Param  NullableVarCharHolder value;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0 || key.isSet == 0) { out.isSet = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            byte[] kb = new byte[key.end - key.start];
            key.buffer.getBytes(key.start, kb);
            byte[] vb = value.isSet == 1 ? new byte[value.end - value.start] : new byte[0];
            if (value.isSet == 1) value.buffer.getBytes(value.start, vb);
            String r = com.dremio.community.udf.json.JsonUtils.set(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8),
                new String(kb, java.nio.charset.StandardCharsets.UTF_8),
                new String(vb, java.nio.charset.StandardCharsets.UTF_8));
            byte[] rb = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(rb.length);
            buf.setBytes(0, rb);
            out.isSet = 1; out.start = 0; out.end = rb.length; out.buffer = buf;
        }
    }

    // ── JSON_DELETE(json, key) → VARCHAR ─────────────────────────────────────
    @FunctionTemplate(name = "json_delete", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class JsonDelete implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Param  NullableVarCharHolder key;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            byte[] kb = new byte[key.end - key.start];
            key.buffer.getBytes(key.start, kb);
            String r = com.dremio.community.udf.json.JsonUtils.delete(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8),
                new String(kb, java.nio.charset.StandardCharsets.UTF_8));
            byte[] rb = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(rb.length);
            buf.setBytes(0, rb);
            out.isSet = 1; out.start = 0; out.end = rb.length; out.buffer = buf;
        }
    }

    // ── JSON_MERGE(json1, json2) → VARCHAR ────────────────────────────────────
    // Shallow merge — json2 keys overwrite json1 keys
    @FunctionTemplate(name = "json_merge", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class JsonMerge implements SimpleFunction {
        @Param  NullableVarCharHolder json1;
        @Param  NullableVarCharHolder json2;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] j1 = new byte[json1.end - json1.start];
            json1.buffer.getBytes(json1.start, j1);
            byte[] j2 = new byte[json2.end - json2.start];
            json2.buffer.getBytes(json2.start, j2);
            String r = com.dremio.community.udf.json.JsonUtils.merge(
                new String(j1, java.nio.charset.StandardCharsets.UTF_8),
                new String(j2, java.nio.charset.StandardCharsets.UTF_8));
            byte[] rb = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(rb.length);
            buf.setBytes(0, rb);
            out.isSet = 1; out.start = 0; out.end = rb.length; out.buffer = buf;
        }
    }

    // ── JSON_PRETTY(json) → VARCHAR ───────────────────────────────────────────
    @FunctionTemplate(name = "json_pretty", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class JsonPretty implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            String r = com.dremio.community.udf.json.JsonUtils.pretty(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8));
            byte[] rb = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(rb.length);
            buf.setBytes(0, rb);
            out.isSet = 1; out.start = 0; out.end = rb.length; out.buffer = buf;
        }
    }

    // ── JSON_MINIFY(json) → VARCHAR ───────────────────────────────────────────
    @FunctionTemplate(name = "json_minify", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class JsonMinify implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            String r = com.dremio.community.udf.json.JsonUtils.minify(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8));
            byte[] rb = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(rb.length);
            buf.setBytes(0, rb);
            out.isSet = 1; out.start = 0; out.end = rb.length; out.buffer = buf;
        }
    }
}
