package com.dremio.community.udf.datetime;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableBigIntHolder;
import org.apache.arrow.vector.holders.NullableTimeStampMilliHolder;

/**
 * Unix epoch UDFs — millisecond precision.
 * Dremio's native TO_UNIX_TIMESTAMP / FROM_UNIXTIME only handle seconds.
 */
public class DateEpochFunctions {

    // ── DT_TO_UNIX_MILLIS ────────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_to_unix_millis",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtToUnixMillis implements SimpleFunction {
        @Param  NullableTimeStampMilliHolder ts;
        @Output NullableBigIntHolder         out;
        public void setup() {}
        public void eval() {
            out.value = ts.value;
            out.isSet = 1;
        }
    }

    // ── DT_FROM_UNIX_MILLIS ──────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_from_unix_millis",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtFromUnixMillis implements SimpleFunction {
        @Param  NullableBigIntHolder         millis;
        @Output NullableTimeStampMilliHolder out;
        public void setup() {}
        public void eval() {
            out.value = millis.value;
            out.isSet = 1;
        }
    }

    // ── DT_TO_UNIX_SECONDS ───────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_to_unix_seconds",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtToUnixSeconds implements SimpleFunction {
        @Param  NullableTimeStampMilliHolder ts;
        @Output NullableBigIntHolder         out;
        public void setup() {}
        public void eval() {
            out.value = ts.value / 1000L;
            out.isSet = 1;
        }
    }

    // ── DT_FROM_UNIX_SECONDS ─────────────────────────────────────────────────

    @FunctionTemplate(name = "dt_from_unix_seconds",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtFromUnixSeconds implements SimpleFunction {
        @Param  NullableBigIntHolder         seconds;
        @Output NullableTimeStampMilliHolder out;
        public void setup() {}
        public void eval() {
            out.value = seconds.value * 1000L;
            out.isSet = 1;
        }
    }
}
