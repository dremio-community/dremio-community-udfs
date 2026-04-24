package com.dremio.community.udf.textsim;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableIntHolder;
import org.apache.arrow.vector.holders.NullableFloat8Holder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class TextEditFunctions {

    /** TEXT_LEVENSHTEIN(s1, s2) → INT — character edit distance */
    @FunctionTemplate(
        name = "text_levenshtein",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextLevenshtein implements SimpleFunction {
        @Param  NullableVarCharHolder s1;
        @Param  NullableVarCharHolder s2;
        @Output NullableIntHolder     out;

        public void setup() {}

        public void eval() {
            byte[] b1 = new byte[s1.end - s1.start];
            s1.buffer.getBytes(s1.start, b1);
            String str1 = new String(b1, java.nio.charset.StandardCharsets.UTF_8);

            byte[] b2 = new byte[s2.end - s2.start];
            s2.buffer.getBytes(s2.start, b2);
            String str2 = new String(b2, java.nio.charset.StandardCharsets.UTF_8);

            out.isSet = 1;
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.levenshtein(str1, str2);
        }
    }

    /** TEXT_LEVENSHTEIN_SIMILARITY(s1, s2) → FLOAT — 0.0 (totally different) to 1.0 (identical) */
    @FunctionTemplate(
        name = "text_levenshtein_similarity",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextLevenshteinSimilarity implements SimpleFunction {
        @Param  NullableVarCharHolder s1;
        @Param  NullableVarCharHolder s2;
        @Output NullableFloat8Holder  out;

        public void setup() {}

        public void eval() {
            byte[] b1 = new byte[s1.end - s1.start];
            s1.buffer.getBytes(s1.start, b1);
            String str1 = new String(b1, java.nio.charset.StandardCharsets.UTF_8);

            byte[] b2 = new byte[s2.end - s2.start];
            s2.buffer.getBytes(s2.start, b2);
            String str2 = new String(b2, java.nio.charset.StandardCharsets.UTF_8);

            out.isSet = 1;
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.levenshteinSimilarity(str1, str2);
        }
    }

    /**
     * TEXT_HAMMING_DISTANCE(s1, s2) → INT
     * Returns the number of positions where characters differ.
     * Returns -1 if the strings have different lengths (undefined for Hamming).
     */
    @FunctionTemplate(
        name = "text_hamming_distance",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextHammingDistance implements SimpleFunction {
        @Param  NullableVarCharHolder s1;
        @Param  NullableVarCharHolder s2;
        @Output NullableIntHolder     out;

        public void setup() {}

        public void eval() {
            byte[] b1 = new byte[s1.end - s1.start];
            s1.buffer.getBytes(s1.start, b1);
            String str1 = new String(b1, java.nio.charset.StandardCharsets.UTF_8);

            byte[] b2 = new byte[s2.end - s2.start];
            s2.buffer.getBytes(s2.start, b2);
            String str2 = new String(b2, java.nio.charset.StandardCharsets.UTF_8);

            out.isSet = 1;
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.hammingDistance(str1, str2);
        }
    }

    /** TEXT_LCS_LENGTH(s1, s2) → INT — length of the longest common subsequence */
    @FunctionTemplate(
        name = "text_lcs_length",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextLcsLength implements SimpleFunction {
        @Param  NullableVarCharHolder s1;
        @Param  NullableVarCharHolder s2;
        @Output NullableIntHolder     out;

        public void setup() {}

        public void eval() {
            byte[] b1 = new byte[s1.end - s1.start];
            s1.buffer.getBytes(s1.start, b1);
            String str1 = new String(b1, java.nio.charset.StandardCharsets.UTF_8);

            byte[] b2 = new byte[s2.end - s2.start];
            s2.buffer.getBytes(s2.start, b2);
            String str2 = new String(b2, java.nio.charset.StandardCharsets.UTF_8);

            out.isSet = 1;
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.lcsLength(str1, str2);
        }
    }

    /**
     * TEXT_PARTIAL_RATIO(s1, s2) → FLOAT
     * Best Levenshtein similarity of the shorter string against any same-length
     * sliding window of the longer string. Useful when one string is a substring
     * or abbreviation of the other.
     */
    @FunctionTemplate(
        name = "text_partial_ratio",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextPartialRatio implements SimpleFunction {
        @Param  NullableVarCharHolder s1;
        @Param  NullableVarCharHolder s2;
        @Output NullableFloat8Holder  out;

        public void setup() {}

        public void eval() {
            byte[] b1 = new byte[s1.end - s1.start];
            s1.buffer.getBytes(s1.start, b1);
            String str1 = new String(b1, java.nio.charset.StandardCharsets.UTF_8);

            byte[] b2 = new byte[s2.end - s2.start];
            s2.buffer.getBytes(s2.start, b2);
            String str2 = new String(b2, java.nio.charset.StandardCharsets.UTF_8);

            out.isSet = 1;
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.partialRatio(str1, str2);
        }
    }
}
