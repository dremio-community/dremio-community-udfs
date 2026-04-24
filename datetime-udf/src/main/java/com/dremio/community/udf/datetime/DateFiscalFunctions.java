package com.dremio.community.udf.datetime;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableDateMilliHolder;
import org.apache.arrow.vector.holders.NullableIntHolder;

/**
 * Fiscal calendar UDFs — all take (DATE, INT fiscal_start_month).
 * start_month: 1=Jan (calendar year), 4=Apr (UK), 7=Jul (AU/US federal), 10=Oct, etc.
 *
 * Naming convention: the fiscal year that BEGINS in start_month of calendar year Y is FY(Y+1).
 * Example (start_month=7): 2023-07-01 → FY2024, 2024-01-15 → FY2024, 2024-07-01 → FY2025.
 */
public class DateFiscalFunctions {

    // ── DT_FISCAL_YEAR ───────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_fiscal_year",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtFiscalYear implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Param  NullableIntHolder       startMonth;
        @Output NullableIntHolder       out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.fiscalYear(d, startMonth.value);
            out.isSet = 1;
        }
    }

    // ── DT_FISCAL_QUARTER ────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_fiscal_quarter",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtFiscalQuarter implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Param  NullableIntHolder       startMonth;
        @Output NullableIntHolder       out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.fiscalQuarter(d, startMonth.value);
            out.isSet = 1;
        }
    }

    // ── DT_FISCAL_MONTH ──────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_fiscal_month",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtFiscalMonth implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Param  NullableIntHolder       startMonth;
        @Output NullableIntHolder       out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.fiscalMonth(d, startMonth.value);
            out.isSet = 1;
        }
    }

    // ── DT_FISCAL_WEEK ───────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_fiscal_week",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtFiscalWeek implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Param  NullableIntHolder       startMonth;
        @Output NullableIntHolder       out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.fiscalWeek(d, startMonth.value);
            out.isSet = 1;
        }
    }

    // ── DT_FISCAL_YEAR_START ─────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_fiscal_year_start",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtFiscalYearStart implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Param  NullableIntHolder       startMonth;
        @Output NullableDateMilliHolder out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            java.time.LocalDate r = com.dremio.community.udf.datetime.DateTimeUtils.fiscalYearStart(d, startMonth.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.toEpochMillis(r);
            out.isSet = 1;
        }
    }

    // ── DT_FISCAL_YEAR_END ───────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_fiscal_year_end",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtFiscalYearEnd implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Param  NullableIntHolder       startMonth;
        @Output NullableDateMilliHolder out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            java.time.LocalDate r = com.dremio.community.udf.datetime.DateTimeUtils.fiscalYearEnd(d, startMonth.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.toEpochMillis(r);
            out.isSet = 1;
        }
    }

    // ── DT_FISCAL_QUARTER_START ──────────────────────────────────────────────

    @FunctionTemplate(name = "dt_fiscal_quarter_start",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtFiscalQuarterStart implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Param  NullableIntHolder       startMonth;
        @Output NullableDateMilliHolder out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            java.time.LocalDate r = com.dremio.community.udf.datetime.DateTimeUtils.fiscalQuarterStart(d, startMonth.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.toEpochMillis(r);
            out.isSet = 1;
        }
    }

    // ── DT_FISCAL_QUARTER_END ────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_fiscal_quarter_end",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtFiscalQuarterEnd implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Param  NullableIntHolder       startMonth;
        @Output NullableDateMilliHolder out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            java.time.LocalDate r = com.dremio.community.udf.datetime.DateTimeUtils.fiscalQuarterEnd(d, startMonth.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.toEpochMillis(r);
            out.isSet = 1;
        }
    }
}
