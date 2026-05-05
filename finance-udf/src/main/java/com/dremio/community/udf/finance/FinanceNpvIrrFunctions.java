/*
 * Dremio Finance UDF — NPV and IRR
 * FIN_NPV(rate, cashflows_csv), FIN_IRR(cashflows_csv)
 *
 * Cash flows passed as a comma-separated VARCHAR, e.g. '-1000,300,400,500'
 * FIN_NPV: first cash flow is period 1 (t=1); add the initial outflow separately if needed.
 * FIN_IRR: first cash flow is t=0 (the initial investment, typically negative).
 */
package com.dremio.community.udf.finance;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableFloat8Holder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class FinanceNpvIrrFunctions {

    // ── FIN_NPV(rate, cashflows_csv) ──────────────────────────────────────────
    @FunctionTemplate(name = "fin_npv", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinNpv implements SimpleFunction {
        @Param  NullableFloat8Holder    rate;
        @Param  NullableVarCharHolder   cashflows;
        @Output NullableFloat8Holder    out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[cashflows.end - cashflows.start];
            cashflows.buffer.getBytes(cashflows.start, b);
            String csv = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.isSet = 1;
            try {
                out.value = com.dremio.community.udf.finance.FinanceMath.npv(rate.value, csv);
            } catch (Exception e) {
                out.value = Double.NaN;
            }
        }
    }

    // ── FIN_IRR(cashflows_csv) ────────────────────────────────────────────────
    @FunctionTemplate(name = "fin_irr", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinIrr implements SimpleFunction {
        @Param  NullableVarCharHolder   cashflows;
        @Output NullableFloat8Holder    out;
        public void setup() {}
        public void eval() {
            byte[] b = new byte[cashflows.end - cashflows.start];
            cashflows.buffer.getBytes(cashflows.start, b);
            String csv = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            out.isSet = 1;
            try {
                out.value = com.dremio.community.udf.finance.FinanceMath.irr(csv);
            } catch (Exception e) {
                out.value = Double.NaN;
            }
        }
    }
}
