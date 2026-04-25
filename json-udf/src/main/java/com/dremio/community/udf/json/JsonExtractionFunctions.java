/*
 * Dremio JSON UDF — Extraction Functions
 * JSON_EXTRACT_STR, JSON_EXTRACT_INT, JSON_EXTRACT_FLOAT, JSON_EXTRACT_BOOL, JSON_EXTRACT_RAW
 *
 * Path syntax: dot-notation — "a.b.c" or "items.0.name" for array indices
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
import org.apache.arrow.vector.holders.NullableFloat8Holder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class JsonExtractionFunctions {

    // ── JSON_EXTRACT_STR(json, path) → VARCHAR ────────────────────────────────
    @FunctionTemplate(name = "json_extract_str", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonExtractStr implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Param  NullableVarCharHolder path;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0 || path.isSet == 0) { out.isSet = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            byte[] pb = new byte[path.end - path.start];
            path.buffer.getBytes(path.start, pb);
            String r = com.dremio.community.udf.json.JsonUtils.extractStr(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8),
                new String(pb, java.nio.charset.StandardCharsets.UTF_8));
            if (r == null) { out.isSet = 0; return; }
            byte[] rb = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(rb.length);
            buf.setBytes(0, rb);
            out.isSet = 1; out.start = 0; out.end = rb.length; out.buffer = buf;
        }
    }

    // ── JSON_EXTRACT_INT(json, path) → BIGINT ────────────────────────────────
    @FunctionTemplate(name = "json_extract_int", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonExtractInt implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Param  NullableVarCharHolder path;
        @Output NullableBigIntHolder  out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0 || path.isSet == 0) { out.isSet = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            byte[] pb = new byte[path.end - path.start];
            path.buffer.getBytes(path.start, pb);
            Long r = com.dremio.community.udf.json.JsonUtils.extractLong(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8),
                new String(pb, java.nio.charset.StandardCharsets.UTF_8));
            if (r == null) { out.isSet = 0; return; }
            out.isSet = 1; out.value = r;
        }
    }

    // ── JSON_EXTRACT_FLOAT(json, path) → FLOAT8 ───────────────────────────────
    @FunctionTemplate(name = "json_extract_float", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonExtractFloat implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Param  NullableVarCharHolder path;
        @Output NullableFloat8Holder  out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0 || path.isSet == 0) { out.isSet = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            byte[] pb = new byte[path.end - path.start];
            path.buffer.getBytes(path.start, pb);
            Double r = com.dremio.community.udf.json.JsonUtils.extractDouble(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8),
                new String(pb, java.nio.charset.StandardCharsets.UTF_8));
            if (r == null) { out.isSet = 0; return; }
            out.isSet = 1; out.value = r;
        }
    }

    // ── JSON_EXTRACT_BOOL(json, path) → BIT ──────────────────────────────────
    @FunctionTemplate(name = "json_extract_bool", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonExtractBool implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Param  NullableVarCharHolder path;
        @Output NullableBitHolder     out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0 || path.isSet == 0) { out.isSet = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            byte[] pb = new byte[path.end - path.start];
            path.buffer.getBytes(path.start, pb);
            Integer r = com.dremio.community.udf.json.JsonUtils.extractBool(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8),
                new String(pb, java.nio.charset.StandardCharsets.UTF_8));
            if (r == null) { out.isSet = 0; return; }
            out.isSet = 1; out.value = r;
        }
    }

    // ── JSON_EXTRACT_RAW(json, path) → VARCHAR (raw JSON) ────────────────────
    @FunctionTemplate(name = "json_extract_raw", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.INTERNAL)
    public static class JsonExtractRaw implements SimpleFunction {
        @Param  NullableVarCharHolder json;
        @Param  NullableVarCharHolder path;
        @Inject ArrowBuf buf;
        @Output NullableVarCharHolder out;
        public void setup() {}
        public void eval() {
            if (json.isSet == 0 || path.isSet == 0) { out.isSet = 0; return; }
            byte[] jb = new byte[json.end - json.start];
            json.buffer.getBytes(json.start, jb);
            byte[] pb = new byte[path.end - path.start];
            path.buffer.getBytes(path.start, pb);
            String r = com.dremio.community.udf.json.JsonUtils.extractRaw(
                new String(jb, java.nio.charset.StandardCharsets.UTF_8),
                new String(pb, java.nio.charset.StandardCharsets.UTF_8));
            if (r == null) { out.isSet = 0; return; }
            byte[] rb = r.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(rb.length);
            buf.setBytes(0, rb);
            out.isSet = 1; out.start = 0; out.end = rb.length; out.buffer = buf;
        }
    }
}
