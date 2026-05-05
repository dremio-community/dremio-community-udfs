/*
 * Dremio Finance UDF — Loan Amortization
 * FIN_AMORT_PAYMENT, FIN_AMORT_INTEREST, FIN_AMORT_PRINCIPAL, FIN_AMORT_BALANCE
 *
 * period is 1-based (period 1 = first payment).
 * rate is the per-period rate (e.g. monthly_rate = annual_rate / 12).
 */
package com.dremio.community.udf.finance;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableFloat8Holder;

public class FinanceAmortFunctions {

    // ── FIN_AMORT_PAYMENT(principal, monthly_rate, nper) ─────────────────────
    @FunctionTemplate(name = "fin_amort_payment", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinAmortPayment implements SimpleFunction {
        @Param  NullableFloat8Holder principal;
        @Param  NullableFloat8Holder monthlyRate;
        @Param  NullableFloat8Holder nper;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.amortPayment(
                principal.value, monthlyRate.value, (int) nper.value);
        }
    }

    // ── FIN_AMORT_INTEREST(principal, monthly_rate, nper, period) ─────────────
    @FunctionTemplate(name = "fin_amort_interest", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinAmortInterest implements SimpleFunction {
        @Param  NullableFloat8Holder principal;
        @Param  NullableFloat8Holder monthlyRate;
        @Param  NullableFloat8Holder nper;
        @Param  NullableFloat8Holder period;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.amortInterest(
                principal.value, monthlyRate.value, (int) nper.value, (int) period.value);
        }
    }

    // ── FIN_AMORT_PRINCIPAL(principal, monthly_rate, nper, period) ────────────
    @FunctionTemplate(name = "fin_amort_principal", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinAmortPrincipal implements SimpleFunction {
        @Param  NullableFloat8Holder principal;
        @Param  NullableFloat8Holder monthlyRate;
        @Param  NullableFloat8Holder nper;
        @Param  NullableFloat8Holder period;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.amortPrincipal(
                principal.value, monthlyRate.value, (int) nper.value, (int) period.value);
        }
    }

    // ── FIN_AMORT_BALANCE(principal, monthly_rate, nper, period) ─────────────
    @FunctionTemplate(name = "fin_amort_balance", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinAmortBalance implements SimpleFunction {
        @Param  NullableFloat8Holder principal;
        @Param  NullableFloat8Holder monthlyRate;
        @Param  NullableFloat8Holder nper;
        @Param  NullableFloat8Holder period;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.amortBalance(
                principal.value, monthlyRate.value, (int) nper.value, (int) period.value);
        }
    }
}
