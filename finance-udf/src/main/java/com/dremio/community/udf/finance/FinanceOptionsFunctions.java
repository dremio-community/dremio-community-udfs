/*
 * Dremio Finance UDF — Black-Scholes Options Pricing
 * FIN_BS_CALL, FIN_BS_PUT
 *
 * S = current stock price, K = strike price, T = time to expiry (years),
 * r = risk-free rate (annual, decimal), sigma = volatility (annual, decimal).
 */
package com.dremio.community.udf.finance;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableFloat8Holder;

public class FinanceOptionsFunctions {

    // ── FIN_BS_CALL(S, K, T, r, sigma) ───────────────────────────────────────
    @FunctionTemplate(name = "fin_bs_call", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinBsCall implements SimpleFunction {
        @Param  NullableFloat8Holder s;
        @Param  NullableFloat8Holder k;
        @Param  NullableFloat8Holder t;
        @Param  NullableFloat8Holder r;
        @Param  NullableFloat8Holder sigma;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.bsCall(
                s.value, k.value, t.value, r.value, sigma.value);
        }
    }

    // ── FIN_BS_PUT(S, K, T, r, sigma) ────────────────────────────────────────
    @FunctionTemplate(name = "fin_bs_put", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinBsPut implements SimpleFunction {
        @Param  NullableFloat8Holder s;
        @Param  NullableFloat8Holder k;
        @Param  NullableFloat8Holder t;
        @Param  NullableFloat8Holder r;
        @Param  NullableFloat8Holder sigma;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.bsPut(
                s.value, k.value, t.value, r.value, sigma.value);
        }
    }
}
