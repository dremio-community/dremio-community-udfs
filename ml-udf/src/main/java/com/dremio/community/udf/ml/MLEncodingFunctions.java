package com.dremio.community.udf.ml;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableIntHolder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

/**
 * Dremio SQL UDFs — Categorical encoding functions.
 *
 *   ML_LABEL_ENCODE(value, categories)  → INT     0-based index in category array; -1 if not found
 *   ML_ONE_HOT_ENCODE(value, categories)→ VARCHAR JSON int array with 1 at matching index, 0 elsewhere
 *   ML_HASH_ENCODE(value, num_buckets)  → INT     stable hash bucket in [0, num_buckets)
 *
 * categories is a JSON string array: '["cat","dog","bird"]'
 *
 * Example:
 *   SELECT
 *     ML_LABEL_ENCODE(country, '["US","UK","DE","FR"]')   AS country_id,
 *     ML_ONE_HOT_ENCODE(color,  '["red","green","blue"]') AS color_vec,
 *     ML_HASH_ENCODE(user_agent, 128)                     AS agent_bucket
 *   FROM events;
 */
public class MLEncodingFunctions {

  // ── ML_LABEL_ENCODE ────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_label_encode",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLLabelEncode implements SimpleFunction {
    @Param  NullableVarCharHolder value;
    @Param  NullableVarCharHolder categories;
    @Output NullableIntHolder     out;

    public void setup() {}

    public void eval() {
      byte[] bv = new byte[value.end - value.start];
      value.buffer.getBytes(value.start, bv);
      byte[] bc = new byte[categories.end - categories.start];
      categories.buffer.getBytes(categories.start, bc);
      String sv = new String(bv, java.nio.charset.StandardCharsets.UTF_8);
      String sc = new String(bc, java.nio.charset.StandardCharsets.UTF_8);
      String[] cats = com.dremio.community.udf.ml.MLUtils.parseStringArray(sc);
      int idx = -1;
      for (int i = 0; i < cats.length; i++) {
        if (cats[i].equals(sv)) { idx = i; break; }
      }
      out.isSet = 1;
      out.value = idx;
    }
  }

  // ── ML_ONE_HOT_ENCODE ──────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_one_hot_encode",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLOneHotEncode implements SimpleFunction {
    @Param   NullableVarCharHolder value;
    @Param   NullableVarCharHolder categories;
    @Inject  ArrowBuf              buf;
    @Output  NullableVarCharHolder out;

    public void setup() {}

    public void eval() {
      byte[] bv = new byte[value.end - value.start];
      value.buffer.getBytes(value.start, bv);
      byte[] bc = new byte[categories.end - categories.start];
      categories.buffer.getBytes(categories.start, bc);
      String sv = new String(bv, java.nio.charset.StandardCharsets.UTF_8);
      String sc = new String(bc, java.nio.charset.StandardCharsets.UTF_8);
      String[] cats = com.dremio.community.udf.ml.MLUtils.parseStringArray(sc);
      int[] vec = new int[cats.length];
      for (int i = 0; i < cats.length; i++) {
        if (cats[i].equals(sv)) { vec[i] = 1; break; }
      }
      String json = com.dremio.community.udf.ml.MLUtils.toJsonInt(vec);
      byte[] bOut = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
      buf = buf.reallocIfNeeded(bOut.length);
      buf.setBytes(0, bOut);
      out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
    }
  }

  // ── ML_HASH_ENCODE ─────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_hash_encode",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLHashEncode implements SimpleFunction {
    @Param  NullableVarCharHolder value;
    @Param  NullableIntHolder     numBuckets;
    @Output NullableIntHolder     out;

    public void setup() {}

    /**
     * Stable, non-negative hash bucket: (hashCode & 0x7FFFFFFF) % numBuckets.
     * Uses Java String hashCode — deterministic across JVM restarts for the same input.
     */
    public void eval() {
      byte[] b = new byte[value.end - value.start];
      value.buffer.getBytes(value.start, b);
      String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
      int hash = (s.hashCode() & 0x7FFFFFFF) % numBuckets.value;
      out.isSet = 1;
      out.value = hash;
    }
  }
}
