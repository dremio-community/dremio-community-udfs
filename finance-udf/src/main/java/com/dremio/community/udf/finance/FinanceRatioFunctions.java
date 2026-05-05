/*
 * Dremio Finance UDF — Financial Ratios & Returns
 * FIN_ROI, FIN_WACC, FIN_GROSS_MARGIN, FIN_NET_MARGIN, FIN_OPERATING_MARGIN,
 * FIN_EPS, FIN_PE_RATIO, FIN_EV, FIN_DEBT_TO_EQUITY, FIN_CURRENT_RATIO, FIN_QUICK_RATIO
 */
package com.dremio.community.udf.finance;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableFloat8Holder;

public class FinanceRatioFunctions {

    // ── FIN_ROI(gain, cost) ───────────────────────────────────────────────────
    @FunctionTemplate(name = "fin_roi", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinRoi implements SimpleFunction {
        @Param  NullableFloat8Holder gain;
        @Param  NullableFloat8Holder cost;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.roi(gain.value, cost.value);
        }
    }

    // ── FIN_WACC(equity_value, debt_value, cost_of_equity, cost_of_debt, tax_rate) ──
    @FunctionTemplate(name = "fin_wacc", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinWacc implements SimpleFunction {
        @Param  NullableFloat8Holder equityValue;
        @Param  NullableFloat8Holder debtValue;
        @Param  NullableFloat8Holder costOfEquity;
        @Param  NullableFloat8Holder costOfDebt;
        @Param  NullableFloat8Holder taxRate;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.wacc(
                equityValue.value, debtValue.value, costOfEquity.value, costOfDebt.value, taxRate.value);
        }
    }

    // ── FIN_GROSS_MARGIN(revenue, cogs) ──────────────────────────────────────
    @FunctionTemplate(name = "fin_gross_margin", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinGrossMargin implements SimpleFunction {
        @Param  NullableFloat8Holder revenue;
        @Param  NullableFloat8Holder cogs;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.grossMargin(revenue.value, cogs.value);
        }
    }

    // ── FIN_NET_MARGIN(net_income, revenue) ───────────────────────────────────
    @FunctionTemplate(name = "fin_net_margin", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinNetMargin implements SimpleFunction {
        @Param  NullableFloat8Holder netIncome;
        @Param  NullableFloat8Holder revenue;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.netMargin(netIncome.value, revenue.value);
        }
    }

    // ── FIN_OPERATING_MARGIN(operating_income, revenue) ──────────────────────
    @FunctionTemplate(name = "fin_operating_margin", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinOperatingMargin implements SimpleFunction {
        @Param  NullableFloat8Holder operatingIncome;
        @Param  NullableFloat8Holder revenue;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.operatingMargin(
                operatingIncome.value, revenue.value);
        }
    }

    // ── FIN_EPS(net_income, shares_outstanding) ───────────────────────────────
    @FunctionTemplate(name = "fin_eps", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinEps implements SimpleFunction {
        @Param  NullableFloat8Holder netIncome;
        @Param  NullableFloat8Holder sharesOutstanding;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.eps(netIncome.value, sharesOutstanding.value);
        }
    }

    // ── FIN_PE_RATIO(price_per_share, eps) ───────────────────────────────────
    @FunctionTemplate(name = "fin_pe_ratio", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinPeRatio implements SimpleFunction {
        @Param  NullableFloat8Holder pricePerShare;
        @Param  NullableFloat8Holder epsValue;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.peRatio(pricePerShare.value, epsValue.value);
        }
    }

    // ── FIN_EV(market_cap, total_debt, cash_equivalents) ─────────────────────
    @FunctionTemplate(name = "fin_ev", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinEv implements SimpleFunction {
        @Param  NullableFloat8Holder marketCap;
        @Param  NullableFloat8Holder totalDebt;
        @Param  NullableFloat8Holder cashEquivalents;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.ev(
                marketCap.value, totalDebt.value, cashEquivalents.value);
        }
    }

    // ── FIN_DEBT_TO_EQUITY(total_debt, total_equity) ─────────────────────────
    @FunctionTemplate(name = "fin_debt_to_equity", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinDebtToEquity implements SimpleFunction {
        @Param  NullableFloat8Holder totalDebt;
        @Param  NullableFloat8Holder totalEquity;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.debtToEquity(totalDebt.value, totalEquity.value);
        }
    }

    // ── FIN_CURRENT_RATIO(current_assets, current_liabilities) ───────────────
    @FunctionTemplate(name = "fin_current_ratio", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinCurrentRatio implements SimpleFunction {
        @Param  NullableFloat8Holder currentAssets;
        @Param  NullableFloat8Holder currentLiabilities;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.currentRatio(
                currentAssets.value, currentLiabilities.value);
        }
    }

    // ── FIN_QUICK_RATIO(cash, receivables, current_liabilities) ──────────────
    @FunctionTemplate(name = "fin_quick_ratio", scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class FinQuickRatio implements SimpleFunction {
        @Param  NullableFloat8Holder cash;
        @Param  NullableFloat8Holder receivables;
        @Param  NullableFloat8Holder currentLiabilities;
        @Output NullableFloat8Holder out;
        public void setup() {}
        public void eval() {
            out.isSet = 1;
            out.value = com.dremio.community.udf.finance.FinanceMath.quickRatio(
                cash.value, receivables.value, currentLiabilities.value);
        }
    }
}
