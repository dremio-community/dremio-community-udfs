package com.dremio.community.udf.datetime;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableBitHolder;
import org.apache.arrow.vector.holders.NullableDateMilliHolder;
import org.apache.arrow.vector.holders.NullableIntHolder;

/**
 * Date arithmetic UDFs.
 * Dremio's DATEDIFF only works in days; these add months, years, age, and leap-year support.
 */
public class DateArithFunctions {

    // ── DT_DIFF_MONTHS ───────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_diff_months",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtDiffMonths implements SimpleFunction {
        @Param  NullableDateMilliHolder start;
        @Param  NullableDateMilliHolder end;
        @Output NullableIntHolder       out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate s = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(start.value);
            java.time.LocalDate e = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(end.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.diffMonths(s, e);
            out.isSet = 1;
        }
    }

    // ── DT_DIFF_YEARS ────────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_diff_years",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtDiffYears implements SimpleFunction {
        @Param  NullableDateMilliHolder start;
        @Param  NullableDateMilliHolder end;
        @Output NullableIntHolder       out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate s = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(start.value);
            java.time.LocalDate e = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(end.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.diffYears(s, e);
            out.isSet = 1;
        }
    }

    // ── DT_AGE_YEARS ─────────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_age_years",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtAgeYears implements SimpleFunction {
        @Param  NullableDateMilliHolder birthDate;
        @Param  NullableDateMilliHolder asOf;
        @Output NullableIntHolder       out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate b = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(birthDate.value);
            java.time.LocalDate a = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(asOf.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.ageYears(b, a);
            out.isSet = 1;
        }
    }

    // ── DT_IS_LEAP_YEAR ──────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_is_leap_year",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtIsLeapYear implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Output NullableBitHolder       out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.isLeapYear(d) ? 1 : 0;
            out.isSet = 1;
        }
    }
}
