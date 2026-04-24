package com.dremio.community.udf.textsim;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableBitHolder;
import org.apache.arrow.vector.holders.NullableFloat8Holder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;
import org.apache.arrow.memory.ArrowBuf;
import javax.inject.Inject;

public class TextTokenFunctions {

    /**
     * TEXT_TOKEN_SORT_RATIO(s1, s2) → FLOAT
     * Sorts both strings' tokens alphabetically then computes Levenshtein similarity.
     * Handles word-order differences: "John Smith" vs "Smith John" → 1.0.
     */
    @FunctionTemplate(
        name = "text_token_sort_ratio",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextTokenSortRatio implements SimpleFunction {
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
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.tokenSortRatio(str1, str2);
        }
    }

    /**
     * TEXT_TOKEN_SET_RATIO(s1, s2) → FLOAT
     * Splits both strings into word sets, then compares subsets intelligently.
     * Handles subset relationships: "Quick Brown Fox" vs "Brown Fox" scores high.
     */
    @FunctionTemplate(
        name = "text_token_set_ratio",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextTokenSetRatio implements SimpleFunction {
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
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.tokenSetRatio(str1, str2);
        }
    }

    /**
     * TEXT_FUZZY_MATCH(s1, s2, threshold) → BIT
     * Returns 1 (true) when Jaro-Winkler similarity >= threshold.
     * Use threshold 0.85–0.92 for name matching; 0.80 for relaxed matching.
     */
    @FunctionTemplate(
        name = "text_fuzzy_match",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextFuzzyMatch implements SimpleFunction {
        @Param  NullableVarCharHolder s1;
        @Param  NullableVarCharHolder s2;
        @Param  NullableFloat8Holder  threshold;
        @Output NullableBitHolder     out;

        public void setup() {}

        public void eval() {
            byte[] b1 = new byte[s1.end - s1.start];
            s1.buffer.getBytes(s1.start, b1);
            String str1 = new String(b1, java.nio.charset.StandardCharsets.UTF_8);

            byte[] b2 = new byte[s2.end - s2.start];
            s2.buffer.getBytes(s2.start, b2);
            String str2 = new String(b2, java.nio.charset.StandardCharsets.UTF_8);

            double sim = com.dremio.community.udf.textsim.TextSimilarityUtils.jaroWinkler(str1, str2);
            out.isSet = 1;
            out.value = sim >= threshold.value ? 1 : 0;
        }
    }

    /**
     * TEXT_IS_SIMILAR(s1, s2, threshold) → BIT
     * Returns 1 (true) when trigram similarity >= threshold.
     * Broader than TEXT_FUZZY_MATCH — good for multi-word strings and addresses.
     */
    @FunctionTemplate(
        name = "text_is_similar",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextIsSimilar implements SimpleFunction {
        @Param  NullableVarCharHolder s1;
        @Param  NullableVarCharHolder s2;
        @Param  NullableFloat8Holder  threshold;
        @Output NullableBitHolder     out;

        public void setup() {}

        public void eval() {
            byte[] b1 = new byte[s1.end - s1.start];
            s1.buffer.getBytes(s1.start, b1);
            String str1 = new String(b1, java.nio.charset.StandardCharsets.UTF_8);

            byte[] b2 = new byte[s2.end - s2.start];
            s2.buffer.getBytes(s2.start, b2);
            String str2 = new String(b2, java.nio.charset.StandardCharsets.UTF_8);

            double sim = com.dremio.community.udf.textsim.TextSimilarityUtils.ngramSimilarity(str1, str2, 3);
            out.isSet = 1;
            out.value = sim >= threshold.value ? 1 : 0;
        }
    }

    /**
     * TEXT_NORMALIZE(s) → VARCHAR
     * Lowercases, removes non-alphanumeric characters, collapses whitespace.
     * Use before similarity functions for consistent comparison.
     */
    @FunctionTemplate(
        name = "text_normalize",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextNormalize implements SimpleFunction {
        @Param  NullableVarCharHolder s;
        @Inject ArrowBuf              buf;
        @Output NullableVarCharHolder out;

        public void setup() {}

        public void eval() {
            byte[] bIn = new byte[s.end - s.start];
            s.buffer.getBytes(s.start, bIn);
            String str = new String(bIn, java.nio.charset.StandardCharsets.UTF_8);

            String result = com.dremio.community.udf.textsim.TextSimilarityUtils.normalize(str);
            byte[] bOut  = result.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet  = 1;
            out.start  = 0;
            out.end    = bOut.length;
            out.buffer = buf;
        }
    }

    /**
     * TEXT_REMOVE_DIACRITICS(s) → VARCHAR
     * Converts accented characters to their ASCII base: é→e, ñ→n, ü→u, etc.
     * Use before similarity functions when comparing multilingual data.
     */
    @FunctionTemplate(
        name = "text_remove_diacritics",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextRemoveDiacritics implements SimpleFunction {
        @Param  NullableVarCharHolder s;
        @Inject ArrowBuf              buf;
        @Output NullableVarCharHolder out;

        public void setup() {}

        public void eval() {
            byte[] bIn = new byte[s.end - s.start];
            s.buffer.getBytes(s.start, bIn);
            String str = new String(bIn, java.nio.charset.StandardCharsets.UTF_8);

            String result = com.dremio.community.udf.textsim.TextSimilarityUtils.removeDiacritics(str);
            byte[] bOut  = result.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf = buf.reallocIfNeeded(bOut.length);
            buf.setBytes(0, bOut);
            out.isSet  = 1;
            out.start  = 0;
            out.end    = bOut.length;
            out.buffer = buf;
        }
    }
}
