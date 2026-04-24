package com.dremio.community.udf.datetime;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableDateMilliHolder;
import org.apache.arrow.vector.holders.NullableIntHolder;

/**
 * Period-end and calendar helper UDFs.
 *
 * Dremio already provides DATE_TRUNC for period STARTS and LAST_DAY for month end,
 * so this file adds only the missing end-of-period functions and day/leap helpers.
 */
public class DateBoundaryFunctions {

    // ── DT_WEEK_END ──────────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_week_end",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtWeekEnd implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Output NullableDateMilliHolder out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            java.time.LocalDate r = com.dremio.community.udf.datetime.DateTimeUtils.weekEnd(d);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.toEpochMillis(r);
            out.isSet = 1;
        }
    }

    // ── DT_QUARTER_END ───────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_quarter_end",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtQuarterEnd implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Output NullableDateMilliHolder out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            java.time.LocalDate r = com.dremio.community.udf.datetime.DateTimeUtils.quarterEnd(d);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.toEpochMillis(r);
            out.isSet = 1;
        }
    }

    // ── DT_YEAR_END ──────────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_year_end",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtYearEnd implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Output NullableDateMilliHolder out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            java.time.LocalDate r = com.dremio.community.udf.datetime.DateTimeUtils.yearEnd(d);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.toEpochMillis(r);
            out.isSet = 1;
        }
    }

    // ── DT_DAYS_IN_MONTH ─────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_days_in_month",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtDaysInMonth implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Output NullableIntHolder       out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.daysInMonth(d);
            out.isSet = 1;
        }
    }
}
