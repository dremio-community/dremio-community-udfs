/*
 * Dremio Finance UDF — Depreciation
 * FIN_DEPRECIATION_SL, FIN_DEPRECIATION_DB, FIN_DEPRECIATION_SYD
 */
package com.dremio.community.udf.finance;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableFloat8Holder;

public class FinanceDepreciationFunctions {

    // ── FIN_DEPRECIATION_SL(cost, salvage, life) ─────────────────────────────
    @FunctionTemplate(name = "fin_depreciation_sl", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinDepreciationSl implements SimpleFunction {
        @Param  NullableFloat8Holder cost;
        @Param  NullableFloat8Holder salvage;
        @Param  NullableFloat8Holder life;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.depreciationSL(
                cost.value, salvage.value, life.value);
        }
    }

    // ── FIN_DEPRECIATION_DB(cost, salvage, life, period) ─────────────────────
    @FunctionTemplate(name = "fin_depreciation_db", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinDepreciationDb implements SimpleFunction {
        @Param  NullableFloat8Holder cost;
        @Param  NullableFloat8Holder salvage;
        @Param  NullableFloat8Holder life;
        @Param  NullableFloat8Holder period;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.depreciationDB(
                cost.value, salvage.value, (int) life.value, (int) period.value);
        }
    }

    // ── FIN_DEPRECIATION_SYD(cost, salvage, life, period) ────────────────────
    @FunctionTemplate(name = "fin_depreciation_syd", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinDepreciationSyd implements SimpleFunction {
        @Param  NullableFloat8Holder cost;
        @Param  NullableFloat8Holder salvage;
        @Param  NullableFloat8Holder life;
        @Param  NullableFloat8Holder period;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.depreciationSYD(
                cost.value, salvage.value, (int) life.value, (int) period.value);
        }
    }
}
