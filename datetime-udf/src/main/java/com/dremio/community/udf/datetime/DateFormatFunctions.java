package com.dremio.community.udf.datetime;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import javax.inject.Inject;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.holders.NullableDateMilliHolder;
import org.apache.arrow.vector.holders.NullableTimeStampMilliHolder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

/**
 * Date/timestamp formatting UDF.
 * Dremio's native DATE_FORMAT is broken/missing; this fills that gap.
 *
 * Supported strftime tokens: %Y %y %m %d %A %a %B %b %j %W %e
 */
public class DateFormatFunctions {

    // ── DT_FORMAT (DATE input) ───────────────────────────────────────────────

    @FunctionTemplate(name = "dt_format",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtFormat implements SimpleFunction {
        @Param  NullableDateMilliHolder date;
        @Param  NullableVarCharHolder   fmt;
        @Inject ArrowBuf                buf;
        @Output NullableVarCharHolder   out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(date.value);
            byte[] fmtBytes = new byte[fmt.end - fmt.start];
            fmt.buffer.getBytes(fmt.start, fmtBytes);
            String pattern = new String(fmtBytes, java.nio.charset.StandardCharsets.UTF_8);
            String result = com.dremio.community.udf.datetime.DateTimeUtils.formatDate(d, pattern);
            byte[] outBytes = result.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(outBytes.length);
            buf.setBytes(0, outBytes);
            out.isSet = 1; out.start = 0; out.end = outBytes.length; out.buffer = buf;
        }
    }

    // ── DT_FORMAT_TS (TIMESTAMP input) ──────────────────────────────────────

    @FunctionTemplate(name = "dt_format_ts",
                      scope = FunctionTemplate.FunctionScope.SIMPLE,
                      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class DtFormatTs implements SimpleFunction {
        @Param  NullableTimeStampMilliHolder ts;
        @Param  NullableVarCharHolder        fmt;
        @Inject ArrowBuf                     buf;
        @Output NullableVarCharHolder        out;
        public void setup() {}
        public void eval() {
            java.time.LocalDate d = com.dremio.community.udf.datetime.DateTimeUtils.toLocalDate(ts.value);
            byte[] fmtBytes = new byte[fmt.end - fmt.start];
            fmt.buffer.getBytes(fmt.start, fmtBytes);
            String pattern = new String(fmtBytes, java.nio.charset.StandardCharsets.UTF_8);
            String result = com.dremio.community.udf.datetime.DateTimeUtils.formatDate(d, pattern);
            byte[] outBytes = result.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(outBytes.length);
            buf.setBytes(0, outBytes);
            out.isSet = 1; out.start = 0; out.end = outBytes.length; out.buffer = buf;
        }
    }
}
