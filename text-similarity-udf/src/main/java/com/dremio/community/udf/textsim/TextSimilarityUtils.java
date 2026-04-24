package com.dremio.community.udf.textsim;

public final class TextSimilarityUtils {

    private TextSimilarityUtils() {}

    // -----------------------------------------------------------------------
    // Edit distance
    // -----------------------------------------------------------------------

    public static int levenshtein(String a, String b) {
        int la = a.length(), lb = b.length();
        if (la == 0) return lb;
        if (lb == 0) return la;
        int[] prev = new int[lb + 1];
        int[] curr = new int[lb + 1];
        for (int j = 0; j <= lb; j++) prev[j] = j;
        for (int i = 1; i <= la; i++) {
            curr[0] = i;
            for (int j = 1; j <= lb; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(prev[j] + 1, curr[j - 1] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        return prev[lb];
    }

    public static double levenshteinSimilarity(String a, String b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        int maxLen = Math.max(a.length(), b.length());
        return 1.0 - (double) levenshtein(a, b) / maxLen;
    }

    /** Returns -1 when strings have different lengths (Hamming is undefined). */
    public static int hammingDistance(String a, String b) {
        if (a.length() != b.length()) return -1;
        int dist = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) dist++;
        }
        return dist;
    }

    public static int lcsLength(String a, String b) {
        int la = a.length(), lb = b.length();
        int[] prev = new int[lb + 1];
        int[] curr = new int[lb + 1];
        for (int i = 1; i <= la; i++) {
            for (int j = 1; j <= lb; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    curr[j] = prev[j - 1] + 1;
                } else {
                    curr[j] = Math.max(prev[j], curr[j - 1]);
                }
            }
            int[] tmp = prev; prev = curr; curr = tmp;
            java.util.Arrays.fill(curr, 0);
        }
        return prev[lb];
    }

    /** Best Levenshtein similarity of the shorter string against any same-length window of the longer. */
    public static double partialRatio(String a, String b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        String shorter = a.length() <= b.length() ? a : b;
        String longer  = a.length() <= b.length() ? b : a;
        if (shorter.length() == longer.length()) return levenshteinSimilarity(shorter, longer);
        double best = 0.0;
        for (int i = 0; i <= longer.length() - shorter.length(); i++) {
            String window = longer.substring(i, i + shorter.length());
            double sim = levenshteinSimilarity(shorter, window);
            if (sim > best) best = sim;
        }
        return best;
    }

    // -----------------------------------------------------------------------
    // Jaro / Jaro-Winkler
    // -----------------------------------------------------------------------

    public static double jaro(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        int l1 = s1.length(), l2 = s2.length();
        if (l1 == 0 || l2 == 0) return 0.0;
        int matchDist = Math.max(Math.max(l1, l2) / 2 - 1, 0);
        boolean[] s1m = new boolean[l1];
        boolean[] s2m = new boolean[l2];
        int matches = 0;
        for (int i = 0; i < l1; i++) {
            int lo = Math.max(0, i - matchDist);
            int hi = Math.min(i + matchDist + 1, l2);
            for (int j = lo; j < hi; j++) {
                if (s2m[j] || s1.charAt(i) != s2.charAt(j)) continue;
                s1m[i] = true;
                s2m[j] = true;
                matches++;
                break;
            }
        }
        if (matches == 0) return 0.0;
        int transpositions = 0, k = 0;
        for (int i = 0; i < l1; i++) {
            if (!s1m[i]) continue;
            while (!s2m[k]) k++;
            if (s1.charAt(i) != s2.charAt(k)) transpositions++;
            k++;
        }
        return (matches / (double) l1
                + matches / (double) l2
                + (matches - transpositions / 2.0) / matches) / 3.0;
    }

    public static double jaroWinkler(String s1, String s2) {
        double j = jaro(s1, s2);
        int prefix = 0;
        int limit = Math.min(4, Math.min(s1.length(), s2.length()));
        for (int i = 0; i < limit; i++) {
            if (s1.charAt(i) == s2.charAt(i)) prefix++;
            else break;
        }
        return j + prefix * 0.1 * (1.0 - j);
    }

    // -----------------------------------------------------------------------
    // N-gram similarity (character-level Jaccard on multisets)
    // -----------------------------------------------------------------------

    public static double ngramSimilarity(String a, String b, int n) {
        if (n <= 0) n = 3;
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        if (a.length() < n && b.length() < n) return a.equalsIgnoreCase(b) ? 1.0 : 0.0;
        java.util.Map<String, Integer> ngramsA = charNgrams(a, n);
        java.util.Map<String, Integer> ngramsB = charNgrams(b, n);
        java.util.Set<String> all = new java.util.HashSet<>(ngramsA.keySet());
        all.addAll(ngramsB.keySet());
        int intersection = 0, union = 0;
        for (String g : all) {
            int ca = ngramsA.getOrDefault(g, 0);
            int cb = ngramsB.getOrDefault(g, 0);
            intersection += Math.min(ca, cb);
            union        += Math.max(ca, cb);
        }
        return union == 0 ? 1.0 : (double) intersection / union;
    }

    private static java.util.Map<String, Integer> charNgrams(String s, int n) {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        for (int i = 0; i <= s.length() - n; i++) {
            String g = s.substring(i, i + n);
            map.put(g, map.getOrDefault(g, 0) + 1);
        }
        return map;
    }

    // -----------------------------------------------------------------------
    // Word-set based similarity
    // -----------------------------------------------------------------------

    private static java.util.Set<String> wordSet(String s) {
        java.util.Set<String> set = new java.util.HashSet<>();
        String norm = s.toLowerCase().replaceAll("[^a-z0-9]", " ").trim();
        for (String t : norm.split("\\s+")) {
            if (!t.isEmpty()) set.add(t);
        }
        return set;
    }

    private static java.util.Map<String, Integer> termFreq(String s) {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        String norm = s.toLowerCase().replaceAll("[^a-z0-9]", " ").trim();
        for (String t : norm.split("\\s+")) {
            if (!t.isEmpty()) map.put(t, map.getOrDefault(t, 0) + 1);
        }
        return map;
    }

    public static double jaccardSimilarity(String a, String b) {
        java.util.Set<String> sa = wordSet(a);
        java.util.Set<String> sb = wordSet(b);
        if (sa.isEmpty() && sb.isEmpty()) return 1.0;
        java.util.Set<String> inter = new java.util.HashSet<>(sa); inter.retainAll(sb);
        java.util.Set<String> union = new java.util.HashSet<>(sa); union.addAll(sb);
        return union.isEmpty() ? 0.0 : (double) inter.size() / union.size();
    }

    public static double diceSimilarity(String a, String b) {
        java.util.Set<String> sa = wordSet(a);
        java.util.Set<String> sb = wordSet(b);
        if (sa.isEmpty() && sb.isEmpty()) return 1.0;
        if (sa.isEmpty() || sb.isEmpty()) return 0.0;
        java.util.Set<String> inter = new java.util.HashSet<>(sa); inter.retainAll(sb);
        return 2.0 * inter.size() / (sa.size() + sb.size());
    }

    public static double overlapCoefficient(String a, String b) {
        java.util.Set<String> sa = wordSet(a);
        java.util.Set<String> sb = wordSet(b);
        if (sa.isEmpty() && sb.isEmpty()) return 1.0;
        if (sa.isEmpty() || sb.isEmpty()) return 0.0;
        java.util.Set<String> inter = new java.util.HashSet<>(sa); inter.retainAll(sb);
        return (double) inter.size() / Math.min(sa.size(), sb.size());
    }

    public static double cosineSimilarityTF(String a, String b) {
        java.util.Map<String, Integer> tfA = termFreq(a);
        java.util.Map<String, Integer> tfB = termFreq(b);
        if (tfA.isEmpty() || tfB.isEmpty()) return 0.0;
        double dot = 0, magA = 0, magB = 0;
        java.util.Set<String> all = new java.util.HashSet<>(tfA.keySet());
        all.addAll(tfB.keySet());
        for (String t : all) {
            double ca = tfA.getOrDefault(t, 0);
            double cb = tfB.getOrDefault(t, 0);
            dot += ca * cb;
        }
        for (int v : tfA.values()) magA += (double) v * v;
        for (int v : tfB.values()) magB += (double) v * v;
        if (magA == 0 || magB == 0) return 0.0;
        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    // -----------------------------------------------------------------------
    // Token-based fuzzy matching
    // -----------------------------------------------------------------------

    private static String sortedTokenString(String s) {
        String norm = s.toLowerCase().replaceAll("[^a-z0-9]", " ").trim();
        String[] tokens = norm.isEmpty() ? new String[0] : norm.split("\\s+");
        java.util.Arrays.sort(tokens);
        return String.join(" ", tokens).trim();
    }

    public static double tokenSortRatio(String a, String b) {
        return levenshteinSimilarity(sortedTokenString(a), sortedTokenString(b));
    }

    public static double tokenSetRatio(String a, String b) {
        java.util.Set<String> sa = wordSet(a);
        java.util.Set<String> sb = wordSet(b);
        java.util.Set<String> inter  = new java.util.HashSet<>(sa); inter.retainAll(sb);
        java.util.Set<String> diffA  = new java.util.HashSet<>(sa); diffA.removeAll(sb);
        java.util.Set<String> diffB  = new java.util.HashSet<>(sb); diffB.removeAll(sa);
        String sInter = sortedSetString(inter);
        String s1 = sInter.trim();
        String s2 = (sInter + " " + sortedSetString(diffA)).trim();
        String s3 = (sInter + " " + sortedSetString(diffB)).trim();
        double r1 = levenshteinSimilarity(s1, s2);
        double r2 = levenshteinSimilarity(s1, s3);
        double r3 = levenshteinSimilarity(s2, s3);
        return Math.max(Math.max(r1, r2), r3);
    }

    private static String sortedSetString(java.util.Set<String> set) {
        String[] arr = set.toArray(new String[0]);
        java.util.Arrays.sort(arr);
        return String.join(" ", arr);
    }

    // -----------------------------------------------------------------------
    // Normalization
    // -----------------------------------------------------------------------

    public static String normalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String removeDiacritics(String s) {
        if (s == null || s.isEmpty()) return "";
        String nfd = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
