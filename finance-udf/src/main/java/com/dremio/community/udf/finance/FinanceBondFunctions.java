/*
 * Dremio Finance UDF — Bond Pricing
 * FIN_BOND_PRICE, FIN_BOND_DURATION, FIN_BOND_YTM
 */
package com.dremio.community.udf.finance;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableFloat8Holder;

public class FinanceBondFunctions {

    // ── FIN_BOND_PRICE(face_value, coupon_rate, ytm, periods) ────────────────
    @FunctionTemplate(name = "fin_bond_price", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinBondPrice implements SimpleFunction {
        @Param  NullableFloat8Holder faceValue;
        @Param  NullableFloat8Holder couponRate;
        @Param  NullableFloat8Holder ytm;
        @Param  NullableFloat8Holder periods;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.bondPrice(
                faceValue.value, couponRate.value, ytm.value, (int) periods.value);
        }
    }

    // ── FIN_BOND_DURATION(face_value, coupon_rate, ytm, periods) ─────────────
    @FunctionTemplate(name = "fin_bond_duration", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinBondDuration implements SimpleFunction {
        @Param  NullableFloat8Holder faceValue;
        @Param  NullableFloat8Holder couponRate;
        @Param  NullableFloat8Holder ytm;
        @Param  NullableFloat8Holder periods;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.bondDuration(
                faceValue.value, couponRate.value, ytm.value, (int) periods.value);
        }
    }

    // ── FIN_BOND_YTM(price, face_value, coupon_rate, periods) ────────────────
    @FunctionTemplate(name = "fin_bond_ytm", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinBondYtm implements SimpleFunction {
        @Param  NullableFloat8Holder price;
        @Param  NullableFloat8Holder faceValue;
        @Param  NullableFloat8Holder couponRate;
        @Param  NullableFloat8Holder periods;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.bondYtm(
                price.value, faceValue.value, couponRate.value, (int) periods.value);
        }
    }
}
