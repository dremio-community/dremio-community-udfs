package com.dremio.community.udf.ml;

/**
 * Static utility methods shared across the Dremio ML UDF library.
 *
 * All methods are static and fully self-contained so they can be called from
 * Dremio's Janino-generated eval() context using fully-qualified class names.
 *
 * Vector / array format: JSON array string, e.g. "[0.1, 0.2, -0.3]"
 * Centroid matrix format: JSON array of arrays, e.g. "[[1.0,2.0],[3.0,4.0]]"
 */
public final class MLUtils {

  private MLUtils() {}

  // ---------------------------------------------------------------------------
  // Parsing
  // ---------------------------------------------------------------------------

  /**
   * Parse a JSON array string into a double[].
   *   "[0.1, 0.2, 0.3]"  or  "[0.1,0.2,0.3]"
   */
  public static double[] parseVector(String json) {
    if (json == null) throw new IllegalArgumentException("Vector input is null");
    String s = json.trim();
    if (s.startsWith("[")) s = s.substring(1);
    if (s.endsWith("]")) s = s.substring(0, s.length() - 1);
    s = s.trim();
    if (s.isEmpty()) return new double[0];
    String[] parts = s.split(",");
    double[] v = new double[parts.length];
    for (int i = 0; i < parts.length; i++) v[i] = Double.parseDouble(parts[i].trim());
    return v;
  }

  /**
   * Parse a JSON array of string values into a String[].
   *   "[\"cat\",\"dog\",\"bird\"]"  or  "[cat,dog,bird]"
   */
  public static String[] parseStringArray(String json) {
    if (json == null) throw new IllegalArgumentException("Category array is null");
    String s = json.trim();
    if (s.startsWith("[")) s = s.substring(1);
    if (s.endsWith("]")) s = s.substring(0, s.length() - 1);
    s = s.trim();
    if (s.isEmpty()) return new String[0];
    // Split on commas not inside quotes (simple tokeniser — no nested structures)
    java.util.List<String> items = new java.util.ArrayList<>();
    boolean inQuote = false;
    StringBuilder cur = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '"') { inQuote = !inQuote; }
      else if (c == ',' && !inQuote) { items.add(cur.toString().trim().replaceAll("^\"|\"$", "")); cur = new StringBuilder(); }
      else { cur.append(c); }
    }
    items.add(cur.toString().trim().replaceAll("^\"|\"$", ""));
    return items.toArray(new String[0]);
  }

  /**
   * Parse a JSON array-of-arrays into a double[][].
   *   "[[1.0,2.0],[3.0,4.0]]"
   */
  public static double[][] parseCentroids(String json) {
    if (json == null) throw new IllegalArgumentException("Centroids input is null");
    String s = json.trim();
    // Strip outer brackets
    if (s.startsWith("[")) s = s.substring(1);
    if (s.endsWith("]")) s = s.substring(0, s.length() - 1);
    s = s.trim();
    if (s.isEmpty()) return new double[0][];
    // Split on "],["
    java.util.List<double[]> centroids = new java.util.ArrayList<>();
    int depth = 0, start = 0;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '[') { if (depth == 0) start = i; depth++; }
      else if (c == ']') { depth--; if (depth == 0) centroids.add(parseVector(s.substring(start, i + 1))); }
    }
    return centroids.toArray(new double[0][]);
  }

  /**
   * Serialize a double[] to a JSON array string: "[0.12,-0.45,0.88]"
   */
  public static String toJson(double[] a) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < a.length; i++) {
      if (i > 0) sb.append(",");
      sb.append(a[i]);
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * Serialize an int[] to a JSON array string: "[0,1,0,0,1]"
   */
  public static String toJsonInt(int[] a) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < a.length; i++) {
      if (i > 0) sb.append(",");
      sb.append(a[i]);
    }
    sb.append("]");
    return sb.toString();
  }

  // ---------------------------------------------------------------------------
  // Vector math
  // ---------------------------------------------------------------------------

  /** Euclidean (L2) distance between two vectors. */
  public static double euclideanDistance(double[] a, double[] b) {
    if (a.length != b.length)
      throw new IllegalArgumentException("Dimension mismatch: " + a.length + " vs " + b.length);
    double sum = 0;
    for (int i = 0; i < a.length; i++) { double d = a[i] - b[i]; sum += d * d; }
    return Math.sqrt(sum);
  }

  /** Dot product of two vectors. */
  public static double dotProduct(double[] a, double[] b) {
    if (a.length != b.length)
      throw new IllegalArgumentException("Dimension mismatch: " + a.length + " vs " + b.length);
    double sum = 0;
    for (int i = 0; i < a.length; i++) sum += a[i] * b[i];
    return sum;
  }

  // ---------------------------------------------------------------------------
  // Activation math
  // ---------------------------------------------------------------------------

  /** Sigmoid: 1 / (1 + exp(-x)) */
  public static double sigmoid(double x) {
    return 1.0 / (1.0 + Math.exp(-x));
  }

  /** ReLU: max(0, x) */
  public static double relu(double x) {
    return x > 0 ? x : 0.0;
  }

  /** Leaky ReLU: x if x > 0, else alpha * x */
  public static double leakyRelu(double x, double alpha) {
    return x > 0 ? x : alpha * x;
  }

  /** ELU: x if x >= 0, else alpha * (exp(x) - 1) */
  public static double elu(double x, double alpha) {
    return x >= 0 ? x : alpha * (Math.exp(x) - 1.0);
  }

  /** Swish: x * sigmoid(x) */
  public static double swish(double x) {
    return x * sigmoid(x);
  }

  /**
   * GELU (Gaussian Error Linear Unit) — fast approximation:
   *   x * 0.5 * (1 + tanh(sqrt(2/π) * (x + 0.044715 * x^3)))
   */
  public static double gelu(double x) {
    double inner = 0.7978845608028654 * (x + 0.044715 * x * x * x);
    return 0.5 * x * (1.0 + Math.tanh(inner));
  }

  // ---------------------------------------------------------------------------
  // Softmax (numerically stable)
  // ---------------------------------------------------------------------------

  /** Softmax over a double[]. Subtracts max for numerical stability. */
  public static double[] softmax(double[] a) {
    if (a.length == 0) return new double[0];
    double maxVal = a[0];
    for (int i = 1; i < a.length; i++) if (a[i] > maxVal) maxVal = a[i];
    double sumExp = 0;
    double[] out = new double[a.length];
    for (int i = 0; i < a.length; i++) { out[i] = Math.exp(a[i] - maxVal); sumExp += out[i]; }
    for (int i = 0; i < a.length; i++) out[i] /= sumExp;
    return out;
  }

  // ---------------------------------------------------------------------------
  // Clustering
  // ---------------------------------------------------------------------------

  /**
   * Return the index of the centroid nearest to point (Euclidean distance).
   * Returns -1 if centroids array is empty.
   */
  public static int nearestCentroidIndex(double[] point, double[][] centroids) {
    int best = -1;
    double bestDist = Double.MAX_VALUE;
    for (int i = 0; i < centroids.length; i++) {
      double d = euclideanDistance(point, centroids[i]);
      if (d < bestDist) { bestDist = d; best = i; }
    }
    return best;
  }

  /**
   * Return the Euclidean distance to the nearest centroid.
   */
  public static double nearestCentroidDistance(double[] point, double[][] centroids) {
    double bestDist = Double.MAX_VALUE;
    for (double[] c : centroids) {
      double d = euclideanDistance(point, c);
      if (d < bestDist) bestDist = d;
    }
    return bestDist;
  }
}
