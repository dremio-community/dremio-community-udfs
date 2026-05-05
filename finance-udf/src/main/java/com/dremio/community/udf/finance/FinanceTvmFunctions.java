/*
 * Dremio Finance UDF — Time Value of Money
 * FIN_PV, FIN_FV, FIN_PMT, FIN_NPER, FIN_RATE
 */
package com.dremio.community.udf.finance;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.Float8Holder;
import org.apache.arrow.vector.holders.NullableFloat8Holder;

public class FinanceTvmFunctions {

    // ── FIN_PV(rate, nper, pmt) ────────────────────────────────────────────────
    @FunctionTemplate(name = "fin_pv", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinPv implements SimpleFunction {
        @Param  NullableFloat8Holder rate;
        @Param  NullableFloat8Holder nper;
        @Param  NullableFloat8Holder pmt;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.pv(rate.value, nper.value, pmt.value, 0);
        }
    }

    // ── FIN_FV(rate, nper, pmt, pv) ───────────────────────────────────────────
    @FunctionTemplate(name = "fin_fv", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinFv implements SimpleFunction {
        @Param  NullableFloat8Holder rate;
        @Param  NullableFloat8Holder nper;
        @Param  NullableFloat8Holder pmt;
        @Param  NullableFloat8Holder pv;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.fv(rate.value, nper.value, pmt.value, pv.value);
        }
    }

    // ── FIN_PMT(rate, nper, pv) ───────────────────────────────────────────────
    @FunctionTemplate(name = "fin_pmt", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinPmt implements SimpleFunction {
        @Param  NullableFloat8Holder rate;
        @Param  NullableFloat8Holder nper;
        @Param  NullableFloat8Holder pv;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.pmt(rate.value, nper.value, pv.value);
        }
    }

    // ── FIN_NPER(rate, pmt, pv) ───────────────────────────────────────────────
    @FunctionTemplate(name = "fin_nper", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinNper implements SimpleFunction {
        @Param  NullableFloat8Holder rate;
        @Param  NullableFloat8Holder pmt;
        @Param  NullableFloat8Holder pv;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.nper(rate.value, pmt.value, pv.value);
        }
    }

    // ── FIN_RATE(nper, pmt, pv) ───────────────────────────────────────────────
    @FunctionTemplate(name = "fin_rate", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinRate implements SimpleFunction {
        @Param  NullableFloat8Holder nper;
        @Param  NullableFloat8Holder pmt;
        @Param  NullableFloat8Holder pv;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.rate(nper.value, pmt.value, pv.value);
        }
    }
}
