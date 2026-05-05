/*
 * Dremio Finance UDF — Interest & Growth
 * FIN_COMPOUND_INTEREST, FIN_SIMPLE_INTEREST, FIN_CAGR
 */
package com.dremio.community.udf.finance;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableFloat8Holder;

public class FinanceInterestFunctions {

    // ── FIN_COMPOUND_INTEREST(principal, annual_rate, periods_per_year, years) ──
    @FunctionTemplate(name = "fin_compound_interest", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinCompoundInterest implements SimpleFunction {
        @Param  NullableFloat8Holder principal;
        @Param  NullableFloat8Holder annualRate;
        @Param  NullableFloat8Holder periodsPerYear;
        @Param  NullableFloat8Holder years;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.compoundInterest(
                principal.value, annualRate.value, periodsPerYear.value, years.value);
        }
    }

    // ── FIN_SIMPLE_INTEREST(principal, rate, time) ────────────────────────────
    @FunctionTemplate(name = "fin_simple_interest", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinSimpleInterest implements SimpleFunction {
        @Param  NullableFloat8Holder principal;
        @Param  NullableFloat8Holder rate;
        @Param  NullableFloat8Holder time;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.simpleInterest(
                principal.value, rate.value, time.value);
        }
    }

    // ── FIN_CAGR(start_value, end_value, years) ───────────────────────────────
    @FunctionTemplate(name = "fin_cagr", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinCagr implements SimpleFunction {
        @Param  NullableFloat8Holder startValue;
        @Param  NullableFloat8Holder endValue;
        @Param  NullableFloat8Holder years;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.cagr(
                startValue.value, endValue.value, years.value);
        }
    }
}
