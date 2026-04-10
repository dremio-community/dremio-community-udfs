package com.dremio.community.udf.ml;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableFloat8Holder;
import org.apache.arrow.vector.holders.NullableIntHolder;

/**
 * Dremio SQL UDFs — Model evaluation metric components.
 *
 * These are per-row component functions — use AVG() to aggregate into final metrics.
 *
 *   ML_SQUARED_ERROR(actual, predicted)          → DOUBLE   (y - ŷ)²         → AVG = MSE
 *   ML_ABS_ERROR(actual, predicted)              → DOUBLE   |y - ŷ|           → AVG = MAE
 *   ML_LOG_LOSS(actual, predicted_prob)          → DOUBLE   binary cross-entropy per row → AVG = log-loss
 *   ML_HUBER_LOSS(actual, predicted, delta)      → DOUBLE   Huber loss per row (robust to outliers)
 *   ML_ACCURACY_HIT(actual, predicted)           → INT      1 if equal, else 0  → AVG = accuracy
 *
 * Example — evaluate a regression model:
 *   SELECT
 *     AVG(ML_SQUARED_ERROR(actual_price, predicted_price))     AS mse,
 *     SQRT(AVG(ML_SQUARED_ERROR(actual_price, predicted_price))) AS rmse,
 *     AVG(ML_ABS_ERROR(actual_price, predicted_price))         AS mae
 *   FROM predictions;
 *
 * Example — evaluate a classifier:
 *   SELECT
 *     AVG(ML_LOG_LOSS(CAST(is_fraud AS DOUBLE), fraud_prob))   AS log_loss,
 *     AVG(ML_ACCURACY_HIT(actual_label, predicted_label))      AS accuracy
 *   FROM classifier_results;
 */
public class MLEvalFunctions {

  // ── ML_SQUARED_ERROR ───────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_squared_error",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLSquaredError implements SimpleFunction {
    @Param  NullableFloat8Holder actual;
    @Param  NullableFloat8Holder predicted;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      double diff = actual.value - predicted.value;
      out.value = diff * diff;
    }
  }

  // ── ML_ABS_ERROR ───────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_abs_error",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLAbsError implements SimpleFunction {
    @Param  NullableFloat8Holder actual;
    @Param  NullableFloat8Holder predicted;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      out.value = Math.abs(actual.value - predicted.value);
    }
  }

  // ── ML_LOG_LOSS ────────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_log_loss",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLLogLoss implements SimpleFunction {
    @Param  NullableFloat8Holder actual;
    @Param  NullableFloat8Holder predictedProb;
    @Output NullableFloat8Holder out;

    public void setup() {}

    /**
     * Binary cross-entropy: -(y*log(p) + (1-y)*log(1-p))
     * actual should be 0.0 or 1.0.  predictedProb is clipped to [1e-15, 1-1e-15] for stability.
     */
    public void eval() {
      double eps = 1e-15;
      double p = Math.max(eps, Math.min(1.0 - eps, predictedProb.value));
      double y = actual.value;
      out.isSet = 1;
      out.value = -(y * Math.log(p) + (1.0 - y) * Math.log(1.0 - p));
    }
  }

  // ── ML_HUBER_LOSS ──────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_huber_loss",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLHuberLoss implements SimpleFunction {
    @Param  NullableFloat8Holder actual;
    @Param  NullableFloat8Holder predicted;
    @Param  NullableFloat8Holder delta;
    @Output NullableFloat8Holder out;

    public void setup() {}

    /**
     * Huber loss: quadratic for |error| <= delta, linear beyond.
     * Robust to outliers compared to MSE — use delta ≈ 1.0 as a starting point.
     *   L = 0.5 * error²                      if |error| <= delta
     *   L = delta * (|error| - 0.5 * delta)   otherwise
     */
    public void eval() {
      double err = Math.abs(actual.value - predicted.value);
      double d = delta.value;
      out.isSet = 1;
      out.value = (err <= d) ? (0.5 * err * err) : (d * (err - 0.5 * d));
    }
  }

  // ── ML_ACCURACY_HIT ────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_accuracy_hit",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLAccuracyHit implements SimpleFunction {
    @Param  NullableIntHolder actual;
    @Param  NullableIntHolder predicted;
    @Output NullableIntHolder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      out.value = (actual.value == predicted.value) ? 1 : 0;
    }
  }
}
