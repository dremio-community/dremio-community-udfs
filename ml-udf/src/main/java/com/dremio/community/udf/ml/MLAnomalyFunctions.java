package com.dremio.community.udf.ml;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableFloat8Holder;
import org.apache.arrow.vector.holders.NullableIntHolder;

/**
 * Dremio SQL UDFs — Anomaly detection and outlier functions.
 *
 *   ML_IQR_OUTLIER(value, q1, q3, multiplier)          → INT     1 if outlier (Tukey fence), else 0
 *   ML_ZSCORE_OUTLIER(value, mean, stddev, threshold)   → INT     1 if |z-score| > threshold, else 0
 *   ML_WINSORIZE(value, lower_bound, upper_bound)       → DOUBLE  clip value to [lower, upper]
 *   ML_ISOLATION_SCORE(value, mean, stddev)             → DOUBLE  anomaly probability in (0, 1)
 *
 * Example — flag outliers in a transaction dataset:
 *   SELECT transaction_id, amount,
 *          ML_IQR_OUTLIER(amount, 45.0, 210.0, 1.5)         AS is_iqr_outlier,
 *          ML_ZSCORE_OUTLIER(amount, 127.5, 88.3, 3.0)      AS is_zscore_outlier,
 *          ML_WINSORIZE(amount, 10.0, 500.0)                 AS amount_winsorized,
 *          ML_ISOLATION_SCORE(amount, 127.5, 88.3)           AS anomaly_prob
 *   FROM transactions;
 */
public class MLAnomalyFunctions {

  // ── ML_IQR_OUTLIER ─────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_iqr_outlier",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLIqrOutlier implements SimpleFunction {
    @Param  NullableFloat8Holder value;
    @Param  NullableFloat8Holder q1;
    @Param  NullableFloat8Holder q3;
    @Param  NullableFloat8Holder multiplier;
    @Output NullableIntHolder    out;

    public void setup() {}

    /**
     * Tukey fence: outlier if value < Q1 - k*IQR  or  value > Q3 + k*IQR
     * Typical multiplier: 1.5 (mild), 3.0 (extreme).
     */
    public void eval() {
      double iqr = q3.value - q1.value;
      double fence = multiplier.value * iqr;
      int isOutlier = (value.value < q1.value - fence || value.value > q3.value + fence) ? 1 : 0;
      out.isSet = 1;
      out.value = isOutlier;
    }
  }

  // ── ML_ZSCORE_OUTLIER ──────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_zscore_outlier",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLZScoreOutlier implements SimpleFunction {
    @Param  NullableFloat8Holder value;
    @Param  NullableFloat8Holder mean;
    @Param  NullableFloat8Holder stddev;
    @Param  NullableFloat8Holder threshold;
    @Output NullableIntHolder    out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      if (stddev.value == 0.0) {
        out.value = 0;
      } else {
        double z = Math.abs((value.value - mean.value) / stddev.value);
        out.value = (z > threshold.value) ? 1 : 0;
      }
    }
  }

  // ── ML_WINSORIZE ───────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_winsorize",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLWinsorize implements SimpleFunction {
    @Param  NullableFloat8Holder value;
    @Param  NullableFloat8Holder lowerBound;
    @Param  NullableFloat8Holder upperBound;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      out.value = Math.max(lowerBound.value, Math.min(upperBound.value, value.value));
    }
  }

  // ── ML_ISOLATION_SCORE ─────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_isolation_score",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLIsolationScore implements SimpleFunction {
    @Param  NullableFloat8Holder value;
    @Param  NullableFloat8Holder mean;
    @Param  NullableFloat8Holder stddev;
    @Output NullableFloat8Holder out;

    public void setup() {}

    /**
     * Gaussian anomaly score: probability that value is anomalous under a normal distribution.
     *   score = 1 - exp(-0.5 * z^2) where z = |value - mean| / stddev
     * Returns a value in (0, 1): values near 1 are likely anomalies.
     * This is a fast scalar approximation — not a full Isolation Forest model.
     */
    public void eval() {
      out.isSet = 1;
      if (stddev.value == 0.0) {
        out.value = (value.value == mean.value) ? 0.0 : 1.0;
      } else {
        double z = (value.value - mean.value) / stddev.value;
        out.value = 1.0 - Math.exp(-0.5 * z * z);
      }
    }
  }
}
