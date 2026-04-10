package com.dremio.community.udf.ml;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableFloat8Holder;

/**
 * Dremio SQL UDFs — Neural network activation functions.
 *
 * All inputs/outputs are DOUBLE (FLOAT8).  NULL input → NULL output.
 *
 *   ML_SIGMOID(x)              → DOUBLE   1 / (1 + exp(-x)), range (0, 1)
 *   ML_RELU(x)                 → DOUBLE   max(0, x)
 *   ML_LEAKY_RELU(x, alpha)    → DOUBLE   x if x > 0 else alpha * x
 *   ML_ELU(x, alpha)           → DOUBLE   x if x >= 0 else alpha * (exp(x) - 1)
 *   ML_SWISH(x)                → DOUBLE   x * sigmoid(x)
 *   ML_GELU(x)                 → DOUBLE   Gaussian Error Linear Unit (fast approx)
 *
 * Example:
 *   SELECT ML_SIGMOID(raw_score),
 *          ML_RELU(hidden_layer_output),
 *          ML_GELU(transformer_activation)
 *   FROM model_outputs;
 */
public class MLActivationFunctions {

  // ── ML_SIGMOID ─────────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_sigmoid",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLSigmoid implements SimpleFunction {
    @Param  NullableFloat8Holder x;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      out.value = com.dremio.community.udf.ml.MLUtils.sigmoid(x.value);
    }
  }

  // ── ML_RELU ────────────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_relu",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLReLU implements SimpleFunction {
    @Param  NullableFloat8Holder x;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      out.value = com.dremio.community.udf.ml.MLUtils.relu(x.value);
    }
  }

  // ── ML_LEAKY_RELU ──────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_leaky_relu",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLLeakyReLU implements SimpleFunction {
    @Param  NullableFloat8Holder x;
    @Param  NullableFloat8Holder alpha;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      out.value = com.dremio.community.udf.ml.MLUtils.leakyRelu(x.value, alpha.value);
    }
  }

  // ── ML_ELU ─────────────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_elu",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLElu implements SimpleFunction {
    @Param  NullableFloat8Holder x;
    @Param  NullableFloat8Holder alpha;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      out.value = com.dremio.community.udf.ml.MLUtils.elu(x.value, alpha.value);
    }
  }

  // ── ML_SWISH ───────────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_swish",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLSwish implements SimpleFunction {
    @Param  NullableFloat8Holder x;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      out.value = com.dremio.community.udf.ml.MLUtils.swish(x.value);
    }
  }

  // ── ML_GELU ────────────────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_gelu",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLGelu implements SimpleFunction {
    @Param  NullableFloat8Holder x;
    @Output NullableFloat8Holder out;

    public void setup() {}

    public void eval() {
      out.isSet = 1;
      out.value = com.dremio.community.udf.ml.MLUtils.gelu(x.value);
    }
  }
}
