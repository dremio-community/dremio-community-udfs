package com.dremio.community.udf.ml;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableFloat8Holder;
import org.apache.arrow.vector.holders.NullableIntHolder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

/**
 * Dremio SQL UDFs — Feature engineering / preprocessing functions.
 *
 *   ML_ZSCORE(value, mean, stddev)                      → DOUBLE  (x - μ) / σ
 *   ML_MIN_MAX_SCALE(value, min, max)                   → DOUBLE  (x - min) / (max - min) → [0, 1]
 *   ML_MIN_MAX_SCALE_RANGE(value, min, max, a, b)       → DOUBLE  scale to [a, b]
 *   ML_ROBUST_SCALE(value, median, iqr)                 → DOUBLE  (x - median) / IQR
 *   ML_BINARIZE(value, threshold)                       → INT     1 if x >= threshold else 0
 *   ML_BIN(value, edges)                                → INT     bucket index (0-based) given JSON edge array
 *   ML_CLIP_VALUE(value, min, max)                      → DOUBLE  clamp scalar to [min, max]
 *   ML_LOG1P(value)                                     → DOUBLE  log(1 + x)  (log-transform for skewed data)
 *
 * Example — prepare features for a model:
 *   SELECT
 *     ML_ZSCORE(age, 35.2, 12.4)           AS age_scaled,
 *     ML_MIN_MAX_SCALE(income, 0, 200000)  AS income_scaled,
 *     ML_BINARIZE(has_churned, 0.5)        AS label,
 *     ML_BIN(credit_score, '[300,580,670,740,850]') AS credit_tier
 *   FROM customers;
 */
public class MLFeatureFunctions {

  // ── ML_ZSCORE ──────────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_zscore",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLZScore implements SimpleFunction {
    @Param  NullableFloat8Holder value;
    @Param  NullableFloat8Holder mean;
    @Param  NullableFloat8Holder stddev;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      if (stddev.value == 0.0) {
        out.value = 0.0;
      } else {
        out.value = (value.value - mean.value) / stddev.value;
      }
    }
  }

  // ── ML_MIN_MAX_SCALE ───────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_min_max_scale",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLMinMaxScale implements SimpleFunction {
    @Param  NullableFloat8Holder value;
    @Param  NullableFloat8Holder min;
    @Param  NullableFloat8Holder max;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      double range = max.value - min.value;
      if (range == 0.0) {
        out.value = 0.0;
      } else {
        out.value = (value.value - min.value) / range;
      }
    }
  }

  // ── ML_MIN_MAX_SCALE_RANGE ─────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_min_max_scale_range",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLMinMaxScaleRange implements SimpleFunction {
    @Param  NullableFloat8Holder value;
    @Param  NullableFloat8Holder min;
    @Param  NullableFloat8Holder max;
    @Param  NullableFloat8Holder newMin;
    @Param  NullableFloat8Holder newMax;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      double range = max.value - min.value;
      if (range == 0.0) {
        out.value = newMin.value;
      } else {
        double scaled = (value.value - min.value) / range;
        out.value = newMin.value + scaled * (newMax.value - newMin.value);
      }
    }
  }

  // ── ML_ROBUST_SCALE ────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_robust_scale",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLRobustScale implements SimpleFunction {
    @Param  NullableFloat8Holder value;
    @Param  NullableFloat8Holder median;
    @Param  NullableFloat8Holder iqr;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      if (iqr.value == 0.0) {
        out.value = 0.0;
      } else {
        out.value = (value.value - median.value) / iqr.value;
      }
    }
  }

  // ── ML_BINARIZE ────────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_binarize",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLBinarize implements SimpleFunction {
    @Param  NullableFloat8Holder value;
    @Param  NullableFloat8Holder threshold;
    @Output NullableIntHolder    out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      out.value = (value.value >= threshold.value) ? 1 : 0;
    }
  }

  // ── ML_BIN ─────────────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_bin",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLBin implements SimpleFunction {
    @Param  NullableFloat8Holder  value;
    @Param  NullableVarCharHolder edges;
    @Output NullableIntHolder     out;

    public void setup() {}

    /**
     * Assigns value to a bucket defined by the edges array.
     * edges = "[e0, e1, e2, ..., eN]" defines N+1 buckets:
     *   bucket 0:  value < e0
     *   bucket 1:  e0 <= value < e1
     *   ...
     *   bucket N:  value >= eN
     * Returns the 0-based bucket index.
     */
    public void eval() {
      byte[] b = new byte[edges.end - edges.start];
      edges.buffer.getBytes(edges.start, b);
      String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
      double[] e = com.dremio.community.udf.ml.MLUtils.parseVector(s);
      double v = value.value;
      int bucket = e.length; // default: last bucket (value >= all edges)
      for (int i = 0; i < e.length; i++) {
        if (v < e[i]) { bucket = i; break; }
      }
      out.isSet = 1;
      out.value = bucket;
    }
  }

  // ── ML_CLIP_VALUE ──────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_clip_value",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLClipValue implements SimpleFunction {
    @Param  NullableFloat8Holder value;
    @Param  NullableFloat8Holder minVal;
    @Param  NullableFloat8Holder maxVal;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      out.value = Math.max(minVal.value, Math.min(maxVal.value, value.value));
    }
  }

  // ── ML_LOG1P ───────────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_log1p",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLLog1p implements SimpleFunction {
    @Param  NullableFloat8Holder value;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      out.value = Math.log1p(value.value);
    }
  }
}
