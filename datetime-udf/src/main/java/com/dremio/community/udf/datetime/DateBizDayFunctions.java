package com.dremio.community.udf.datetime;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableBitHolder;
import org.apache.arrow.vector.holders.NullableDateMilliHolder;
import org.apache.arrow.vector.holders.NullableIntHolder;

/**
 * Business day (weekday) UDFs — no holiday calendar support, weekday math only.
 */
public class DateBizDayFunctions {

    // ── DT_IS_WEEKDAY ────────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_is_weekday",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtIsWeekday implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Output NullableBitHolder       out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.isWeekday(d) ? 1 : 0;
            out.isSet = 1;
        }
    }

    // ── DT_BIZDAYS_BETWEEN ───────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_bizdays_between",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtBizdaysBetween implements SimpleFunction {
        @Param  NullableDateMilliHolder start;
        @Param  NullableDateMilliHolder end;
        @Output NullableIntHolder       out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate s = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(start.value);
            java.time.LocalDate e = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(end.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.bizdaysBetween(s, e);
            out.isSet = 1;
        }
    }

    // ── DT_ADD_BIZDAYS ───────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_add_bizdays",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtAddBizdays implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Param  NullableIntHolder       n;
        @Output NullableDateMilliHolder out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            java.time.LocalDate r = com.dremio.community.udf.datetime.DateTimeUtils.addBizdays(d, n.value);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.toEpochMillis(r);
            out.isSet = 1;
        }
    }

    // ── DT_NEXT_WEEKDAY ──────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_next_weekday",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtNextWeekday implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Output NullableDateMilliHolder out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            java.time.LocalDate r = com.dremio.community.udf.datetime.DateTimeUtils.nextWeekday(d);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.toEpochMillis(r);
            out.isSet = 1;
        }
    }

    // ── DT_PREV_WEEKDAY ──────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_prev_weekday",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtPrevWeekday implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Output NullableDateMilliHolder out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            java.time.LocalDate r = com.dremio.community.udf.datetime.DateTimeUtils.prevWeekday(d);
            out.value = com.dremio.community.udf.datetime.DateTimeUtils.toEpochMillis(r);
            out.isSet = 1;
        }
    }
}
