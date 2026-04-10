package com.dremio.community.udf.ml;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableFloat8Holder;
import org.apache.arrow.vector.holders.NullableIntHolder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

/**
 * Dremio SQL UDFs — Model scoring / inference functions.
 *
 * Vectors are passed as VARCHAR JSON-array strings: "[0.1, 0.2, 0.3]"
 *
 *   ML_LINEAR_SCORE(features, weights, bias)   → DOUBLE   dot(f, w) + bias
 *   ML_LOGISTIC_SCORE(features, weights, bias) → DOUBLE   sigmoid(linear_score)
 *   ML_SOFTMAX_SCORE(scores)                   → VARCHAR  softmax probabilities as JSON array
 *   ML_ARGMAX(scores)                          → INT      index of maximum value
 *   ML_ARGMIN(scores)                          → INT      index of minimum value
 *
 * Example — logistic regression classifier:
 *   SELECT customer_id,
 *          ML_LOGISTIC_SCORE(features, '[0.42,-0.18,0.91,0.05]', -0.33) AS churn_prob
 *   FROM customer_features;
 *
 * Example — multi-class softmax classification:
 *   SELECT id,
 *          ML_ARGMAX(ML_SOFTMAX_SCORE(raw_logits)) AS predicted_class
 *   FROM classifier_output;
 */
public class MLScoringFunctions {

  // ── ML_LINEAR_SCORE ────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_linear_score",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLLinearScore implements SimpleFunction {
    @Param  NullableVarCharHolder features;
    @Param  NullableVarCharHolder weights;
    @Param  NullableFloat8Holder  bias;
    @Output NullableFloat8Holder  out;

    public void setup() {}

    public void eval() {
      byte[] bf = new byte[features.end - features.start];
      features.buffer.getBytes(features.start, bf);
      byte[] bw = new byte[weights.end - weights.start];
      weights.buffer.getBytes(weights.start, bw);
      String sf = new String(bf, java.nio.charset.StandardCharsets.UTF_8);
      String sw = new String(bw, java.nio.charset.StandardCharsets.UTF_8);
      double[] f = com.dremio.community.udf.ml.MLUtils.parseVector(sf);
      double[] w = com.dremio.community.udf.ml.MLUtils.parseVector(sw);
      out.isSet = 1;
      out.value = com.dremio.community.udf.ml.MLUtils.dotProduct(f, w) + bias.value;
    }
  }

  // ── ML_LOGISTIC_SCORE ──────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_logistic_score",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLLogisticScore implements SimpleFunction {
    @Param  NullableVarCharHolder features;
    @Param  NullableVarCharHolder weights;
    @Param  NullableFloat8Holder  bias;
    @Output NullableFloat8Holder  out;

    public void setup() {}

    public void eval() {
      byte[] bf = new byte[features.end - features.start];
      features.buffer.getBytes(features.start, bf);
      byte[] bw = new byte[weights.end - weights.start];
      weights.buffer.getBytes(weights.start, bw);
      String sf = new String(bf, java.nio.charset.StandardCharsets.UTF_8);
      String sw = new String(bw, java.nio.charset.StandardCharsets.UTF_8);
      double[] f = com.dremio.community.udf.ml.MLUtils.parseVector(sf);
      double[] w = com.dremio.community.udf.ml.MLUtils.parseVector(sw);
      double linear = com.dremio.community.udf.ml.MLUtils.dotProduct(f, w) + bias.value;
      out.isSet = 1;
      out.value = com.dremio.community.udf.ml.MLUtils.sigmoid(linear);
    }
  }

  // ── ML_SOFTMAX_SCORE ───────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_softmax_score",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLSoftmaxScore implements SimpleFunction {
    @Param   NullableVarCharHolder scores;
    @Inject  ArrowBuf              buf;
    @Output  NullableVarCharHolder out;

    public void setup() {}

    public void eval() {
      byte[] bIn = new byte[scores.end - scores.start];
      scores.buffer.getBytes(scores.start, bIn);
      String s = new String(bIn, java.nio.charset.StandardCharsets.UTF_8);
      double[] raw = com.dremio.community.udf.ml.MLUtils.parseVector(s);
      double[] sm  = com.dremio.community.udf.ml.MLUtils.softmax(raw);
      String json  = com.dremio.community.udf.ml.MLUtils.toJson(sm);
      byte[] bOut  = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
      buf = buf.reallocIfNeeded(bOut.length);
      buf.setBytes(0, bOut);
      out.isSet = 1; out.start = 0; out.end = bOut.length; out.buffer = buf;
    }
  }

  // ── ML_ARGMAX ──────────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_argmax",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLArgmax implements SimpleFunction {
    @Param  NullableVarCharHolder scores;
    @Output NullableIntHolder     out;

    public void setup() {}

    public void eval() {
      byte[] b = new byte[scores.end - scores.start];
      scores.buffer.getBytes(scores.start, b);
      String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
      double[] v = com.dremio.community.udf.ml.MLUtils.parseVector(s);
      int best = 0;
      for (int i = 1; i < v.length; i++) if (v[i] > v[best]) best = i;
      out.isSet = 1;
      out.value = best;
    }
  }

  // ── ML_ARGMIN ──────────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_argmin",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLArgmin implements SimpleFunction {
    @Param  NullableVarCharHolder scores;
    @Output NullableIntHolder     out;

    public void setup() {}

    public void eval() {
      byte[] b = new byte[scores.end - scores.start];
      scores.buffer.getBytes(scores.start, b);
      String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
      double[] v = com.dremio.community.udf.ml.MLUtils.parseVector(s);
      int best = 0;
      for (int i = 1; i < v.length; i++) if (v[i] < v[best]) best = i;
      out.isSet = 1;
      out.value = best;
    }
  }
}
