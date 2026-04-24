package com.dremio.community.udf.textsim;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableFloat8Holder;
import org.apache.arrow.vector.holders.NullableIntHolder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

public class TextSimilarityFunctions {

    /** TEXT_JARO(s1, s2) → FLOAT — Jaro similarity (0-1) */
    @FunctionTemplate(
        name = "text_jaro",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextJaro implements SimpleFunction {
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
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.jaro(str1, str2);
        }
    }

    /**
     * TEXT_JARO_WINKLER(s1, s2) → FLOAT
     * Jaro-Winkler similarity — boosts score for matching common prefixes (up to 4 chars).
     * Best general-purpose fuzzy string matcher for names and short strings.
     */
    @FunctionTemplate(
        name = "text_jaro_winkler",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextJaroWinkler implements SimpleFunction {
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
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.jaroWinkler(str1, str2);
        }
    }

    /** TEXT_TRIGRAM_SIMILARITY(s1, s2) → FLOAT — 3-gram character Jaccard similarity */
    @FunctionTemplate(
        name = "text_trigram_similarity",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextTrigramSimilarity implements SimpleFunction {
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
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.ngramSimilarity(str1, str2, 3);
        }
    }

    /** TEXT_BIGRAM_SIMILARITY(s1, s2) → FLOAT — 2-gram character Jaccard similarity */
    @FunctionTemplate(
        name = "text_bigram_similarity",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextBigramSimilarity implements SimpleFunction {
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
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.ngramSimilarity(str1, str2, 2);
        }
    }

    /**
     * TEXT_NGRAM_SIMILARITY(s1, s2, n) → FLOAT — character n-gram Jaccard similarity.
     * Use n=2 (bigram) for short strings, n=3 (trigram) for medium strings.
     */
    @FunctionTemplate(
        name = "text_ngram_similarity",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextNgramSimilarity implements SimpleFunction {
        @Param  NullableVarCharHolder s1;
        @Param  NullableVarCharHolder s2;
        @Param  NullableIntHolder     n;
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
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.ngramSimilarity(str1, str2, n.value);
        }
    }

    /**
     * TEXT_JACCARD_SIMILARITY(s1, s2) → FLOAT — word-set Jaccard: |A∩B| / |A∪B|.
     * Good for comparing bag-of-words documents or multi-word strings.
     */
    @FunctionTemplate(
        name = "text_jaccard_similarity",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextJaccardSimilarity implements SimpleFunction {
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
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.jaccardSimilarity(str1, str2);
        }
    }

    /**
     * TEXT_DICE_SIMILARITY(s1, s2) → FLOAT — Sørensen–Dice coefficient on word sets: 2|A∩B| / (|A|+|B|).
     * Similar to Jaccard but gives more weight to shared words.
     */
    @FunctionTemplate(
        name = "text_dice_similarity",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextDiceSimilarity implements SimpleFunction {
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
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.diceSimilarity(str1, str2);
        }
    }

    /**
     * TEXT_OVERLAP_COEFFICIENT(s1, s2) → FLOAT — |A∩B| / min(|A|,|B|).
     * Returns 1.0 when the smaller word set is fully contained in the larger.
     */
    @FunctionTemplate(
        name = "text_overlap_coefficient",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextOverlapCoefficient implements SimpleFunction {
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
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.overlapCoefficient(str1, str2);
        }
    }

    /**
     * TEXT_COSINE_SIMILARITY(s1, s2) → FLOAT — TF cosine similarity on word vectors.
     * Handles repeated words correctly. Useful for longer text comparison.
     */
    @FunctionTemplate(
        name = "text_cosine_similarity",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class TextCosineSimilarity implements SimpleFunction {
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
            out.value = com.dremio.community.udf.textsim.TextSimilarityUtils.cosineSimilarityTF(str1, str2);
        }
    }
}
