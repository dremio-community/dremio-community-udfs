package com.dremio.community.udf.textsim;

import org.junit.Test;
import static org.junit.Assert.*;

public class TextSimilarityUtilsTest {

    private static final double EPS = 1e-9;
    private static final double LOOSE = 1e-3;

    // -----------------------------------------------------------------------
    // levenshtein
    // -----------------------------------------------------------------------

    @Test public void levenshteinIdentical()      { assertEquals(0, TextSimilarityUtils.levenshtein("hello", "hello")); }
    @Test public void levenshteinEmptyBoth()       { assertEquals(0, TextSimilarityUtils.levenshtein("", "")); }
    @Test public void levenshteinEmptyLeft()       { assertEquals(5, TextSimilarityUtils.levenshtein("", "hello")); }
    @Test public void levenshteinEmptyRight()      { assertEquals(5, TextSimilarityUtils.levenshtein("hello", "")); }
    @Test public void levenshteinSingleSubst()     { assertEquals(1, TextSimilarityUtils.levenshtein("cat", "bat")); }
    @Test public void levenshteinSingleInsert()    { assertEquals(1, TextSimilarityUtils.levenshtein("cat", "cast")); }
    @Test public void levenshteinSingleDelete()    { assertEquals(1, TextSimilarityUtils.levenshtein("cast", "cat")); }
    @Test public void levenshteinKitten()          { assertEquals(3, TextSimilarityUtils.levenshtein("kitten", "sitting")); }
    @Test public void levenshteinSaturday()        { assertEquals(3, TextSimilarityUtils.levenshtein("saturday", "sunday")); }
    @Test public void levenshteinCompleteDiff()    { assertEquals(5, TextSimilarityUtils.levenshtein("abcde", "fghij")); }

    // -----------------------------------------------------------------------
    // levenshteinSimilarity
    // -----------------------------------------------------------------------

    @Test public void levSimIdentical()     { assertEquals(1.0, TextSimilarityUtils.levenshteinSimilarity("hello", "hello"), EPS); }
    @Test public void levSimBothEmpty()     { assertEquals(1.0, TextSimilarityUtils.levenshteinSimilarity("", ""), EPS); }
    @Test public void levSimOneEmpty()      { assertEquals(0.0, TextSimilarityUtils.levenshteinSimilarity("", "hello"), EPS); }
    @Test public void levSimKitten()        { assertEquals(1.0 - 3.0/7.0, TextSimilarityUtils.levenshteinSimilarity("kitten", "sitting"), EPS); }
    @Test public void levSimRange()         {
        double s = TextSimilarityUtils.levenshteinSimilarity("abc", "xyz");
        assertTrue("should be in [0,1]", s >= 0.0 && s <= 1.0);
    }

    // -----------------------------------------------------------------------
    // hammingDistance
    // -----------------------------------------------------------------------

    @Test public void hammingIdentical()      { assertEquals(0,  TextSimilarityUtils.hammingDistance("abc", "abc")); }
    @Test public void hammingOneDiff()        { assertEquals(1,  TextSimilarityUtils.hammingDistance("abc", "axc")); }
    @Test public void hammingAllDiff()        { assertEquals(3,  TextSimilarityUtils.hammingDistance("abc", "xyz")); }
    @Test public void hammingUnequalLengths() { assertEquals(-1, TextSimilarityUtils.hammingDistance("abc", "ab")); }
    @Test public void hammingEmpty()          { assertEquals(0,  TextSimilarityUtils.hammingDistance("", "")); }
    @Test public void hammingKarr()           { assertEquals(3,  TextSimilarityUtils.hammingDistance("karolin", "kathrin")); }

    // -----------------------------------------------------------------------
    // lcsLength
    // -----------------------------------------------------------------------

    @Test public void lcsIdentical()   { assertEquals(5, TextSimilarityUtils.lcsLength("hello", "hello")); }
    @Test public void lcsBothEmpty()   { assertEquals(0, TextSimilarityUtils.lcsLength("", "")); }
    @Test public void lcsOneEmpty()    { assertEquals(0, TextSimilarityUtils.lcsLength("", "hello")); }
    @Test public void lcsKnown()       { assertEquals(4, TextSimilarityUtils.lcsLength("ABCBDAB", "BDCAB")); }
    @Test public void lcsNoOverlap()   { assertEquals(0, TextSimilarityUtils.lcsLength("abc", "xyz")); }
    @Test public void lcsSubstring()   { assertEquals(3, TextSimilarityUtils.lcsLength("abcde", "ace")); }

    // -----------------------------------------------------------------------
    // partialRatio
    // -----------------------------------------------------------------------

    @Test public void partialRatioIdentical()   { assertEquals(1.0, TextSimilarityUtils.partialRatio("hello", "hello"), EPS); }
    @Test public void partialRatioBothEmpty()   { assertEquals(1.0, TextSimilarityUtils.partialRatio("", ""), EPS); }
    @Test public void partialRatioOneEmpty()    { assertEquals(0.0, TextSimilarityUtils.partialRatio("", "hello"), EPS); }
    @Test public void partialRatioSubstring()   {
        // "abc" is a perfect substring of "xyzabcdef" → should score 1.0
        assertEquals(1.0, TextSimilarityUtils.partialRatio("abc", "xyzabcdef"), EPS);
    }
    @Test public void partialRatioShortInLong() {
        // shorter matched near-perfectly somewhere in longer
        double s = TextSimilarityUtils.partialRatio("John", "John Smith");
        assertTrue("should be high", s > 0.9);
    }

    // -----------------------------------------------------------------------
    // jaro
    // -----------------------------------------------------------------------

    @Test public void jaroIdentical()     { assertEquals(1.0, TextSimilarityUtils.jaro("MARTHA", "MARTHA"), EPS); }
    @Test public void jaroBothEmpty()     { assertEquals(1.0, TextSimilarityUtils.jaro("", ""), EPS); }
    @Test public void jaroOneEmpty()      { assertEquals(0.0, TextSimilarityUtils.jaro("", "MARTHA"), EPS); }
    @Test public void jaroMarthaMarhta()  {
        assertEquals(0.9444, TextSimilarityUtils.jaro("MARTHA", "MARHTA"), LOOSE);
    }
    @Test public void jaroDixonDicksonx() {
        double s = TextSimilarityUtils.jaro("DIXON", "DICKSONX");
        assertTrue("Dixon/Dicksonx jaro should be ~0.767", s > 0.7 && s < 0.85);
    }
    @Test public void jaroNoOverlap()     {
        double s = TextSimilarityUtils.jaro("ABCDE", "FGHIJ");
        assertEquals(0.0, s, EPS);
    }

    // -----------------------------------------------------------------------
    // jaroWinkler
    // -----------------------------------------------------------------------

    @Test public void jwIdentical()     { assertEquals(1.0, TextSimilarityUtils.jaroWinkler("MARTHA", "MARTHA"), EPS); }
    @Test public void jwMarthaMarhta()  {
        double jw = TextSimilarityUtils.jaroWinkler("MARTHA", "MARHTA");
        assertTrue("JW >= Jaro for common prefix", jw >= TextSimilarityUtils.jaro("MARTHA", "MARHTA"));
    }
    @Test public void jwCommonPrefix()  {
        double jw  = TextSimilarityUtils.jaroWinkler("ABCDEF", "ABCXYZ");
        double j   = TextSimilarityUtils.jaro("ABCDEF", "ABCXYZ");
        assertTrue("JW > Jaro when 3-char prefix matches", jw > j);
    }
    @Test public void jwRange()         {
        double s = TextSimilarityUtils.jaroWinkler("hello", "world");
        assertTrue("should be in [0,1]", s >= 0.0 && s <= 1.0);
    }

    // -----------------------------------------------------------------------
    // ngramSimilarity
    // -----------------------------------------------------------------------

    @Test public void trigramIdentical()  { assertEquals(1.0, TextSimilarityUtils.ngramSimilarity("hello", "hello", 3), EPS); }
    @Test public void trigramBothEmpty()  { assertEquals(1.0, TextSimilarityUtils.ngramSimilarity("", "", 3), EPS); }
    @Test public void trigramOneEmpty()   { assertEquals(0.0, TextSimilarityUtils.ngramSimilarity("", "hello", 3), EPS); }
    @Test public void trigramNoOverlap()  { assertEquals(0.0, TextSimilarityUtils.ngramSimilarity("abc", "xyz", 3), EPS); }
    @Test public void trigramPartial()    {
        // "colour" vs "color" share trigrams "col" and "olo"
        double s = TextSimilarityUtils.ngramSimilarity("colour", "color", 3);
        assertTrue("colour/color should have trigram overlap", s > 0.0 && s < 1.0);
    }
    @Test public void bigramSimilarity()  {
        double s = TextSimilarityUtils.ngramSimilarity("cat", "cats", 2);
        assertTrue("bigram: cat/cats should be similar", s > 0.5);
    }

    // -----------------------------------------------------------------------
    // jaccardSimilarity (word-level)
    // -----------------------------------------------------------------------

    @Test public void jaccardIdentical()    { assertEquals(1.0, TextSimilarityUtils.jaccardSimilarity("the cat sat", "the cat sat"), EPS); }
    @Test public void jaccardBothEmpty()    { assertEquals(1.0, TextSimilarityUtils.jaccardSimilarity("", ""), EPS); }
    @Test public void jaccardNoOverlap()    { assertEquals(0.0, TextSimilarityUtils.jaccardSimilarity("apple banana", "orange grape"), EPS); }
    @Test public void jaccardPartial()      {
        // "the cat" vs "the dog" — 1 shared ("the"), union=3 → 1/3
        assertEquals(1.0/3.0, TextSimilarityUtils.jaccardSimilarity("the cat", "the dog"), LOOSE);
    }
    @Test public void jaccardSubset()       {
        // "cat sat" vs "cat sat mat" — 2 shared, union=3 → 2/3
        assertEquals(2.0/3.0, TextSimilarityUtils.jaccardSimilarity("cat sat", "cat sat mat"), LOOSE);
    }
    @Test public void jaccardCaseInsensitive() {
        assertEquals(1.0, TextSimilarityUtils.jaccardSimilarity("Hello World", "hello world"), EPS);
    }

    // -----------------------------------------------------------------------
    // diceSimilarity (word-level)
    // -----------------------------------------------------------------------

    @Test public void diceIdentical()    { assertEquals(1.0, TextSimilarityUtils.diceSimilarity("the quick fox", "the quick fox"), EPS); }
    @Test public void diceBothEmpty()    { assertEquals(1.0, TextSimilarityUtils.diceSimilarity("", ""), EPS); }
    @Test public void diceNoOverlap()    { assertEquals(0.0, TextSimilarityUtils.diceSimilarity("abc", "xyz"), EPS); }
    @Test public void dicePartial()      {
        // "a b" vs "a c": inter=1, |A|=2, |B|=2 → 2*1/(2+2) = 0.5
        assertEquals(0.5, TextSimilarityUtils.diceSimilarity("a b", "a c"), LOOSE);
    }

    // -----------------------------------------------------------------------
    // overlapCoefficient (word-level)
    // -----------------------------------------------------------------------

    @Test public void overlapIdentical()  { assertEquals(1.0, TextSimilarityUtils.overlapCoefficient("cat dog", "cat dog"), EPS); }
    @Test public void overlapBothEmpty()  { assertEquals(1.0, TextSimilarityUtils.overlapCoefficient("", ""), EPS); }
    @Test public void overlapSubset()     {
        // "cat" is fully in "cat dog" → overlap=1/min(1,2)=1.0
        assertEquals(1.0, TextSimilarityUtils.overlapCoefficient("cat", "cat dog"), EPS);
    }
    @Test public void overlapNoOverlap()  { assertEquals(0.0, TextSimilarityUtils.overlapCoefficient("abc", "xyz"), EPS); }

    // -----------------------------------------------------------------------
    // cosineSimilarityTF (word TF)
    // -----------------------------------------------------------------------

    @Test public void cosineIdentical()  { assertEquals(1.0, TextSimilarityUtils.cosineSimilarityTF("hello world", "hello world"), EPS); }
    @Test public void cosineOrthogonal() { assertEquals(0.0, TextSimilarityUtils.cosineSimilarityTF("apple", "orange"), EPS); }
    @Test public void cosineOneEmpty()   { assertEquals(0.0, TextSimilarityUtils.cosineSimilarityTF("", "hello"), EPS); }
    @Test public void cosinePartial()    {
        double s = TextSimilarityUtils.cosineSimilarityTF("cat sat on mat", "cat mat");
        assertTrue("partial overlap cosine should be in (0,1)", s > 0.0 && s < 1.0);
    }

    // -----------------------------------------------------------------------
    // tokenSortRatio
    // -----------------------------------------------------------------------

    @Test public void tokenSortReordered() {
        // Word order shouldn't matter
        assertEquals(1.0, TextSimilarityUtils.tokenSortRatio("John Smith", "Smith John"), EPS);
    }
    @Test public void tokenSortIdentical() { assertEquals(1.0, TextSimilarityUtils.tokenSortRatio("hello world", "hello world"), EPS); }
    @Test public void tokenSortDifferent() {
        double s = TextSimilarityUtils.tokenSortRatio("apple", "orange");
        assertTrue("should be < 1.0", s < 1.0);
    }
    @Test public void tokenSortCaseInsensitive() {
        assertEquals(1.0, TextSimilarityUtils.tokenSortRatio("HELLO WORLD", "world hello"), EPS);
    }

    // -----------------------------------------------------------------------
    // tokenSetRatio
    // -----------------------------------------------------------------------

    @Test public void tokenSetSubset() {
        // "Brown Fox" is a subset of "Quick Brown Fox"
        double s = TextSimilarityUtils.tokenSetRatio("Quick Brown Fox", "Brown Fox");
        assertTrue("subset should score high", s > 0.9);
    }
    @Test public void tokenSetIdentical() { assertEquals(1.0, TextSimilarityUtils.tokenSetRatio("hello world", "hello world"), EPS); }
    @Test public void tokenSetReordered() {
        double s = TextSimilarityUtils.tokenSetRatio("apple banana cherry", "cherry banana apple");
        assertEquals(1.0, s, LOOSE);
    }

    // -----------------------------------------------------------------------
    // normalize
    // -----------------------------------------------------------------------

    @Test public void normalizeBasic()      { assertEquals("hello world", TextSimilarityUtils.normalize("Hello, World!")); }
    @Test public void normalizeNumbers()    { assertEquals("abc 123", TextSimilarityUtils.normalize("ABC 123")); }
    @Test public void normalizePunctuation(){ assertEquals("hello world", TextSimilarityUtils.normalize("hello...world")); }
    @Test public void normalizeWhitespace() { assertEquals("hello world", TextSimilarityUtils.normalize("  hello   world  ")); }
    @Test public void normalizeEmpty()      { assertEquals("", TextSimilarityUtils.normalize("")); }

    // -----------------------------------------------------------------------
    // removeDiacritics
    // -----------------------------------------------------------------------

    @Test public void diacriticsBasic()   { assertEquals("cafe", TextSimilarityUtils.removeDiacritics("café")); }
    @Test public void diacriticsNtilde()  { assertEquals("senor", TextSimilarityUtils.removeDiacritics("señor")); }
    @Test public void diacriticsUmlaut()  { assertEquals("uber", TextSimilarityUtils.removeDiacritics("über")); }
    @Test public void diacriticsNone()    { assertEquals("hello", TextSimilarityUtils.removeDiacritics("hello")); }
    @Test public void diacriticsEmpty()   { assertEquals("", TextSimilarityUtils.removeDiacritics("")); }
    @Test public void diacriticsMulti()   {
        String result = TextSimilarityUtils.removeDiacritics("résumé");
        assertEquals("resume", result);
    }
}
