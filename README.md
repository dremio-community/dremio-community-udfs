# Dremio Community UDFs

Community-built SQL UDF libraries for [Dremio](https://www.dremio.com/) — adding scalar and aggregate functions not available in Dremio's standard function library.

Each UDF library is a self-contained JAR that installs into `jars/3rdparty/` and immediately extends Dremio's SQL engine with new functions.

---

## Libraries

| Library | Functions | Description | Status |
|---------|-----------|-------------|--------|
| [Geospatial UDF](geo-udf/) | 100 | 77 ST_ geometry functions + 23 H3 hexagonal grid functions | ✅ 28/28 live tests |
| [Vector UDF](vector-udf/) | 26 | Vector similarity, distance, arithmetic, and transformation functions | ✅ 59/59 unit + 12/12 live tests |

---

## Quick Install

```bash
# Geospatial UDF
cd geo-udf
./install.sh --docker try-dremio

# Vector UDF
cd vector-udf
./install.sh --docker try-dremio
```

Restart Dremio after installing. Functions are immediately available in SQL.

---

## Library Overview

### [Geospatial UDF](geo-udf/)

**100 geospatial functions** in a single JAR — the most complete open-source geospatial UDF library for Dremio. Combines JTS-based geometry functions with Uber's H3 hexagonal grid indexing.

```sql
-- Find the H3 hex cell for a point at resolution 8
SELECT H3_AsText(H3_FromGeomPoint(ST_GeomFromText('POINT(-122.4194 37.7749)'), 8));
-- → '8828308281fffff'

-- All H3 cells covering a polygon at resolution 6
SELECT H3_Polyfill(ST_GeomFromText('POLYGON((-122.4 37.8,-122.3 37.8,-122.3 37.7,-122.4 37.7,-122.4 37.8))'), 6);

-- Geodesic distance between two cities (meters)
SELECT CAST(ST_GeodesicLengthWGS84(ST_GeomFromText('LINESTRING(-73.93 40.73,-87.62 41.87)')) AS INT);
-- → 1149380

-- Geometry type inspection
SELECT ST_GeometryType(ST_GeomFromText('POLYGON((0 0,1 0,1 1,0 0))'));
-- → 'ST_POLYGON'
```

| Category | Functions |
|----------|-----------|
| Constructors | `ST_GeomFromText`, `ST_GeomFromEWKT`, `ST_GeomFromWKB`, `ST_GeomFromEWKB`, `ST_GeomFromGeoJson`, `ST_Point`, `ST_PointSrid` |
| Accessors | `ST_X`, `ST_Y`, `ST_XMin`, `ST_XMax`, `ST_YMin`, `ST_YMax`, `ST_Area`, `ST_Length`, `ST_Perimeter`, `ST_GeometryType`, `ST_StartPoint`, `ST_EndPoint`, `ST_PointN`, `ST_ExteriorRing`, `ST_InteriorRingN`, `ST_NumGeometries`, `ST_IsClosed`, `ST_IsSimple`, `ST_IsEmpty`, `ST_IsCollection`, `ST_IsValid`, `ST_IsValidReason`, `ST_Azimuth`, `ST_Relate`, `ST_RelateMatrix` |
| Predicates | `ST_Within`, `ST_Contains`, `ST_Intersects`, `ST_Touches`, `ST_Overlaps`, `ST_Equals`, `ST_Disjoint`, `ST_Crosses`, `ST_DWithin` |
| Operations | `ST_Union`, `ST_Intersection`, `ST_Difference`, `ST_SymDifference`, `ST_Buffer`, `ST_Centroid`, `ST_ConvexHull`, `ST_ConcaveHull`, `ST_Simplify`, `ST_SimplifyPreserveTopology`, `ST_Collect` |
| Transforms | `ST_TransformToProj4`, `ST_TransformToSrid`, `ST_TransformFromProj4ToSrid` |
| Geodesic | `ST_GeodesicAreaWGS84`, `ST_GeodesicLengthWGS84` |
| Aggregates | `ST_UnionAggregate`, `ST_CollectAggregate` |
| Serialization | `ST_AsText`, `ST_AsEWKT`, `ST_AsBinary`, `ST_AsGeoJson` |
| H3 Grid | `H3_FromLongLat`, `H3_FromText`, `H3_AsText`, `H3_FromGeomPoint`, `H3_FromGeomPoly`, `H3_Boundary`, `H3_Center`, `H3_Resolution`, `H3_ToParent`, `H3_ToChildren`, `H3_ToCenterChild`, `H3_KRing`, `H3_HexRing`, `H3_KRingDistances`, `H3_Polyfill`, `H3_Compact`, `H3_Uncompact`, `H3_Wrap`, `H3_Distance`, `H3_IsValid`, `H3_IsPentagon` |

**Key features:** 100 functions · JTS geometry engine · H3 hexagonal indexing (Uber) · proj4j CRS transforms · aggregate functions · EWKB/WKB/WKT/GeoJSON I/O · geodesic area + length · fully shaded (no external deps to manage)

---

### [Vector UDF](vector-udf/)

**26 vector math functions** for running semantic search and similarity queries directly in Dremio SQL. Store embeddings as JSON `VARCHAR` columns in Iceberg or any Dremio-accessible table — no vector database required.

```sql
-- Semantic search: top-10 most similar documents
SELECT id, text,
       COSINE_SIMILARITY(embedding, '[0.12, -0.45, 0.88, ...]') AS score
FROM my_catalog.embeddings
ORDER BY score DESC
LIMIT 10;

-- Cluster by normalized vectors
SELECT id, VECTOR_NORMALIZE(embedding) AS unit_vec
FROM my_catalog.embeddings;
```

| Category | Functions |
|----------|-----------|
| Similarity / Distance | `COSINE_SIMILARITY`, `COSINE_DISTANCE`, `L2_DISTANCE`, `L2_DISTANCE_SQUARED`, `DOT_PRODUCT`, `L1_DISTANCE`, `CHEBYSHEV_DISTANCE`, `MINKOWSKI_DISTANCE`, `VECTOR_DISTANCE` |
| Arithmetic | `VECTOR_ADD`, `VECTOR_SUBTRACT`, `VECTOR_MULTIPLY`, `VECTOR_SCALE`, `VECTOR_CONCAT`, `VECTOR_LERP` |
| Scalar Reductions | `VECTOR_NORM`, `VECTOR_DIMS`, `VECTOR_SUM`, `VECTOR_MAX_ELEMENT`, `VECTOR_MIN_ELEMENT` |
| Transformations | `VECTOR_NORMALIZE`, `VECTOR_SOFTMAX`, `VECTOR_CLIP`, `VECTOR_SLICE` |
| Utility | `VECTOR_ELEMENT_AT`, `VECTOR_IS_VALID` |

**Key features:** 26 scalar UDFs · JSON-encoded vectors (`VARCHAR`) · cosine / L2 / L1 / L∞ / Minkowski · Hadamard product · LERP · softmax · clip · Matryoshka slice · no external dependencies

---

## Requirements

| Requirement | Details |
|-------------|---------|
| Dremio OSS or Enterprise | 26.x (tested against 26.0.5) |
| Java 11+ | Required on the build machine if building from source |
| Maven 3.8+ | Required for source builds only |
| Docker | Optional — used by `install.sh` for Docker-based Dremio |

---

## Building from Source

Each library includes a `pom.xml` and builds with Maven:

```bash
# Geospatial UDF
cd geo-udf
docker run --rm -v "$(pwd)":/project -v ~/.m2:/root/.m2 -w /project \
  maven:3.9-eclipse-temurin-11 \
  mvn package -DskipTests -Dcheckstyle.skip=true -Ddetekt.skip=true -Dmaven.javadoc.skip=true

# Vector UDF
cd vector-udf
docker run --rm -v "$(pwd)":/project -v ~/.m2:/root/.m2 -w /project \
  maven:3.9-eclipse-temurin-11 \
  mvn package -DskipTests
```

Pre-built JARs are included in each library's `jars/` directory for direct installation without a build step.

---

## Repository Structure

```
dremio-community-udfs/
├── geo-udf/         — Geospatial UDF library (100 functions: ST_ + H3)
│   ├── jars/        — Pre-built JAR
│   ├── src/         — Java source (JTS + H3 + GeodesicUtils)
│   ├── pom.xml
│   └── install.sh
├── vector-udf/      — Vector UDF library (26 functions)
│   ├── jars/        — Pre-built JAR
│   ├── src/         — Java source
│   ├── pom.xml
│   └── install.sh
└── .github/
    └── workflows/   — CI per library
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for how to report bugs, request features, and submit pull requests.

---

## License

Apache License 2.0

*Built by Mark Shainman*
