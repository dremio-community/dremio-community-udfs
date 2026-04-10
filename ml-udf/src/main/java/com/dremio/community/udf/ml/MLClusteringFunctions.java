package com.dremio.community.udf.ml;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import org.apache.arrow.vector.holders.NullableFloat8Holder;
import org.apache.arrow.vector.holders.NullableIntHolder;
import org.apache.arrow.vector.holders.NullableVarCharHolder;

/**
 * Dremio SQL UDFs — Clustering / segment assignment functions.
 *
 * Vectors are VARCHAR JSON-array strings.
 * Centroids are a VARCHAR JSON array-of-arrays: "[[1.0,2.0],[3.0,4.0]]"
 *
 *   ML_KMEANS_ASSIGN(point, centroids)    → INT     0-based index of the nearest centroid
 *   ML_KMEANS_DISTANCE(point, centroids)  → DOUBLE  Euclidean distance to the nearest centroid
 *
 * Workflow — pre-train K-Means externally (Python/sklearn), export centroids as JSON,
 * then score every row in Dremio SQL:
 *
 *   -- Store centroids as a SQL literal or a small lookup table
 *   SELECT customer_id,
 *          ML_KMEANS_ASSIGN(feature_vector,
 *            '[[0.1,0.2,0.3],[0.8,0.7,0.6],[0.4,0.5,0.4]]') AS segment,
 *          ML_KMEANS_DISTANCE(feature_vector,
 *            '[[0.1,0.2,0.3],[0.8,0.7,0.6],[0.4,0.5,0.4]]') AS centroid_dist
 *   FROM customer_embeddings;
 */
public class MLClusteringFunctions {

  // ── ML_KMEANS_ASSIGN ───────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_kmeans_assign",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLKMeansAssign implements SimpleFunction {
    @Param  NullableVarCharHolder point;
    @Param  NullableVarCharHolder centroids;
    @Output NullableIntHolder     out;

    public void setup() {}

    public void eval() {
      byte[] bp = new byte[point.end - point.start];
      point.buffer.getBytes(point.start, bp);
      byte[] bc = new byte[centroids.end - centroids.start];
      centroids.buffer.getBytes(centroids.start, bc);
      String sp = new String(bp, java.nio.charset.StandardCharsets.UTF_8);
      String sc = new String(bc, java.nio.charset.StandardCharsets.UTF_8);
      double[]   pt = com.dremio.community.udf.ml.MLUtils.parseVector(sp);
      double[][] cs = com.dremio.community.udf.ml.MLUtils.parseCentroids(sc);
      out.isSet = 1;
      out.value = com.dremio.community.udf.ml.MLUtils.nearestCentroidIndex(pt, cs);
    }
  }

  // ── ML_KMEANS_DISTANCE ─────────────────────────────────────────────────────

  @FunctionTemplate(
      name = "ml_kmeans_distance",
      scope = FunctionTemplate.FunctionScope.SIMPLE,
      nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class MLKMeansDistance implements SimpleFunction {
    @Param  NullableVarCharHolder point;
    @Param  NullableVarCharHolder centroids;
    @Output NullableFloat8Holder  out;

    public void setup() {}

    public void eval() {
      byte[] bp = new byte[point.end - point.start];
      point.buffer.getBytes(point.start, bp);
      byte[] bc = new byte[centroids.end - centroids.start];
      centroids.buffer.getBytes(centroids.start, bc);
      String sp = new String(bp, java.nio.charset.StandardCharsets.UTF_8);
      String sc = new String(bc, java.nio.charset.StandardCharsets.UTF_8);
      double[]   pt = com.dremio.community.udf.ml.MLUtils.parseVector(sp);
      double[][] cs = com.dremio.community.udf.ml.MLUtils.parseCentroids(sc);
      out.isSet = 1;
      out.value = com.dremio.community.udf.ml.MLUtils.nearestCentroidDistance(pt, cs);
    }
  }
}
