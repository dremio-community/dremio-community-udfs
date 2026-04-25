# Dremio Community UDFs

Community-built SQL UDF libraries for [Dremio](https://www.dremio.com/) — adding scalar functions not available in Dremio's pre-packaged function library.

Each UDF library is a self-contained JAR that installs into `jars/3rdparty/` and immediately extends Dremio's SQL engine with new functions.

---

## Libraries

| Library | Functions | Description | Status |
|---------|-----------|-------------|--------|
| [Geospatial UDF](geo-udf/) | 100 | 77 ST_ geometry functions + 23 H3 hexagonal grid functions | ✅ 28/28 live tests |
| [Vector UDF](vector-udf/) | 26 | Vector similarity, distance, arithmetic, and transformation functions | ✅ 59/59 unit + 12/12 live tests |
| [ML UDF](ml-udf/) | 32 | Classical ML: activation, scoring, feature engineering, encoding, clustering, anomaly detection, evaluation | ✅ 40/40 live tests |
| [PII UDF](pii-udf/) | 44 | PII detection, masking, extraction, and tokenization for 15 data types | ✅ 39/39 live tests |
| [Text Similarity UDF](text-similarity-udf/) | 20 | Edit distance, Jaro-Winkler, n-gram, Jaccard, Dice, token fuzzy matching, normalization | ✅ 84/84 unit tests |
| [Date/Time UDF](datetime-udf/) | 24 | Fiscal calendar, business days, period end boundaries, diff in months/years, epoch millis, date formatting | ✅ 67/67 unit + 28/28 live tests |

---

## Quick Install

```bash
# Copy any JAR to Dremio's third-party directory and restart
cp geo-udf/jars/dremio-geo-udf-1.0.0.jar                    /opt/dremio/jars/3rdparty/
cp vector-udf/jars/dremio-vector-udf-1.0.0-SNAPSHOT.jar     /opt/dremio/jars/3rdparty/
cp ml-udf/jars/dremio-ml-udf-1.0.0.jar                      /opt/dremio/jars/3rdparty/
cp pii-udf/jars/dremio-pii-udf-1.0.0.jar                    /opt/dremio/jars/3rdparty/
cp text-similarity-udf/jars/dremio-text-similarity-udf-1.0.0.jar /opt/dremio/jars/3rdparty/
cp datetime-udf/jars/dremio-datetime-udf-1.0.0.jar               /opt/dremio/jars/3rdparty/
```

**Docker:**
```bash
docker cp geo-udf/jars/dremio-geo-udf-1.0.0.jar                    try-dremio:/opt/dremio/jars/3rdparty/
docker cp vector-udf/jars/dremio-vector-udf-1.0.0-SNAPSHOT.jar     try-dremio:/opt/dremio/jars/3rdparty/
docker cp ml-udf/jars/dremio-ml-udf-1.0.0.jar                      try-dremio:/opt/dremio/jars/3rdparty/
docker cp pii-udf/jars/dremio-pii-udf-1.0.0.jar                    try-dremio:/opt/dremio/jars/3rdparty/
docker cp text-similarity-udf/jars/dremio-text-similarity-udf-1.0.0.jar try-dremio:/opt/dremio/jars/3rdparty/
docker cp datetime-udf/jars/dremio-datetime-udf-1.0.0.jar          try-dremio:/opt/dremio/jars/3rdparty/
docker restart try-dremio
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

### [ML UDF](ml-udf/)

**32 classical machine learning functions** for scoring models, engineering features, detecting anomalies, and evaluating predictions — all in SQL. Train in Python/sklearn/XGBoost; deploy feature logic and model scoring directly in Dremio.

```sql
-- Logistic regression: score every customer for churn
SELECT customer_id,
       ML_LOGISTIC_SCORE(feature_vector, '[0.42,-0.18,0.91,0.05]', CAST(-0.33 AS DOUBLE)) AS churn_prob
FROM customer_features
ORDER BY churn_prob DESC;

-- Feature engineering pipeline
SELECT customer_id,
       ML_ZSCORE(age, 35.4, 12.1)            AS age_scaled,
       ML_MIN_MAX_SCALE(income, 0, 200000)   AS income_scaled,
       ML_BINARIZE(has_churned, 0.5)         AS label
FROM customers;

-- K-Means segment assignment (centroids exported from sklearn)
SELECT customer_id,
       ML_KMEANS_ASSIGN(feature_vector, '[[0.1,0.8],[0.9,0.1],[0.5,0.5]]') AS segment
FROM customer_embeddings;

-- Evaluate a classifier
SELECT AVG(ML_LOG_LOSS(CAST(actual AS DOUBLE), predicted_prob)) AS log_loss,
       AVG(ML_ACCURACY_HIT(actual_label, predicted_label))      AS accuracy
FROM results;
```

| Category | Functions |
|----------|-----------|
| Activation | `ML_SIGMOID`, `ML_RELU`, `ML_LEAKY_RELU`, `ML_ELU`, `ML_SWISH`, `ML_GELU` |
| Scoring | `ML_LINEAR_SCORE`, `ML_LOGISTIC_SCORE`, `ML_SOFTMAX_SCORE`, `ML_ARGMAX`, `ML_ARGMIN` |
| Feature Engineering | `ML_ZSCORE`, `ML_MIN_MAX_SCALE`, `ML_MIN_MAX_SCALE_RANGE`, `ML_ROBUST_SCALE`, `ML_BINARIZE`, `ML_BIN`, `ML_CLIP_VALUE`, `ML_LOG1P` |
| Encoding | `ML_LABEL_ENCODE`, `ML_ONE_HOT_ENCODE`, `ML_HASH_ENCODE` |
| Clustering | `ML_KMEANS_ASSIGN`, `ML_KMEANS_DISTANCE` |
| Anomaly Detection | `ML_IQR_OUTLIER`, `ML_ZSCORE_OUTLIER`, `ML_WINSORIZE`, `ML_ISOLATION_SCORE` |
| Evaluation | `ML_SQUARED_ERROR`, `ML_ABS_ERROR`, `ML_LOG_LOSS`, `ML_HUBER_LOSS`, `ML_ACCURACY_HIT` |

**Key features:** 32 scalar UDFs · pure Java (no external deps) · logistic/linear regression scoring · softmax classification · K-Means assignment · Tukey + z-score anomaly detection · MSE/MAE/log-loss/Huber evaluation metrics

---

### [PII UDF](pii-udf/)

**44 PII functions** for detecting, masking, extracting, and tokenizing personally identifiable information directly in Dremio SQL. Covers 15 data types with checksum-validated detection and format-preserving masking.

```sql
-- Classify every row for PII type
SELECT value, PII_TYPE(value) AS pii_type, PII_SCORE(value) AS score
FROM raw_data;

-- Mask sensitive columns before sharing
SELECT
    MASK_EMAIL(email)               AS email,
    MASK_PHONE(phone)               AS phone,
    MASK_CREDIT_CARD(card_number)   AS card,
    MASK_SSN(ssn)                   AS ssn
FROM customers;

-- Extract PII from unstructured text
SELECT
    log_id,
    EXTRACT_EMAIL(message)      AS email_found,
    EXTRACT_IPV4(message)       AS ip_found,
    EXTRACT_ALL_PII(message)    AS all_pii_json
FROM application_logs
WHERE IS_PII(message) = 1;

-- Pseudonymize for cross-system joins
SELECT PII_TOKENIZE(email) AS token, SUM(revenue) AS total
FROM transactions GROUP BY token;
```

| Category | Functions |
|----------|-----------|
| Detection | `IS_EMAIL`, `IS_PHONE`, `IS_SSN`, `IS_CREDIT_CARD`, `IS_IBAN`, `IS_IPV4`, `IS_IPV6`, `IS_ZIP`, `IS_DOB`, `IS_PASSPORT`, `IS_VIN`, `IS_NPI`, `IS_EIN`, `IS_MAC`, `IS_URL`, `IS_PII` |
| Classification | `PII_TYPE`, `PII_SCORE` |
| Masking | `MASK_EMAIL`, `MASK_PHONE`, `MASK_SSN`, `MASK_CREDIT_CARD`, `MASK_IBAN`, `MASK_IPV4`, `MASK_NAME`, `MASK_DOB`, `MASK_CUSTOM`, `PII_REDACT` |
| Extraction | `EXTRACT_EMAIL`, `EXTRACT_PHONE`, `EXTRACT_SSN`, `EXTRACT_CREDIT_CARD`, `EXTRACT_IBAN`, `EXTRACT_IPV4`, `EXTRACT_IPV6`, `EXTRACT_URL`, `EXTRACT_ALL_PII` |
| Hash / Tokenization | `PII_SHA256`, `PII_SHA256` (salted), `PII_MD5`, `PII_TOKENIZE` |

**Key features:** 44 scalar UDFs · pure Java (no external deps) · Luhn/IBAN mod-97/VIN check digit validation · format-preserving masking · free-text extraction · salted SHA-256 · stable tokens for pseudonymization

---

### [Text Similarity UDF](text-similarity-udf/)

**20 text similarity functions** for fuzzy matching, entity resolution, and string distance in Dremio SQL. Covers the algorithms Dremio doesn't have natively (`SOUNDEX` and `REGEXP_*` are built in; this library adds everything else). No external runtime dependencies — pure Java 11.

```sql
-- Name fuzzy matching: resolve Salesforce contacts to CRM records
SELECT s.Id, c.customer_id
FROM salesforce.Contact s
JOIN iceberg.crm.customers c
  ON TEXT_FUZZY_MATCH(s.Name, c.full_name, 0.88) = 1;

-- Handle word-order variation: "John Smith" = "Smith John"
SELECT TEXT_TOKEN_SORT_RATIO('John Smith', 'Smith John');   -- 1.0

-- Normalize + strip accents before comparison
SELECT TEXT_LEVENSHTEIN(
  TEXT_NORMALIZE(TEXT_REMOVE_DIACRITICS(a)),
  TEXT_NORMALIZE(TEXT_REMOVE_DIACRITICS(b))
) FROM my_table;

-- Address similarity
SELECT TEXT_TRIGRAM_SIMILARITY('123 Main Street', '123 Main St'); -- 0.667
```

| Category | Functions |
|---|---|
| Edit Distance | `TEXT_LEVENSHTEIN`, `TEXT_LEVENSHTEIN_SIMILARITY`, `TEXT_HAMMING_DISTANCE`, `TEXT_LCS_LENGTH`, `TEXT_PARTIAL_RATIO` |
| Character Similarity | `TEXT_JARO`, `TEXT_JARO_WINKLER`, `TEXT_TRIGRAM_SIMILARITY`, `TEXT_BIGRAM_SIMILARITY`, `TEXT_NGRAM_SIMILARITY` |
| Word-Set Similarity | `TEXT_JACCARD_SIMILARITY`, `TEXT_DICE_SIMILARITY`, `TEXT_OVERLAP_COEFFICIENT`, `TEXT_COSINE_SIMILARITY` |
| Token Fuzzy Matching | `TEXT_TOKEN_SORT_RATIO`, `TEXT_TOKEN_SET_RATIO`, `TEXT_FUZZY_MATCH`, `TEXT_IS_SIMILAR` |
| Normalization | `TEXT_NORMALIZE`, `TEXT_REMOVE_DIACRITICS` |

**Key features:** 20 scalar UDFs · pure Java 11 (no external deps) · Levenshtein / Hamming / LCS · Jaro-Winkler · character n-gram · word-set Jaccard / Dice / Overlap / Cosine TF · token sort/set ratio · threshold-based boolean match gates · diacritic removal

---

### [Date/Time UDF](datetime-udf/)

**24 date and time functions** that fill the gaps left by Dremio's native `DATE_TRUNC`, `LAST_DAY`, `DATEDIFF` (days only), and `TO_UNIX_TIMESTAMP` (seconds only). Covers fiscal calendars, business day math, period end boundaries, date diff in months/years, millisecond epoch conversions, and `DATE_FORMAT`.

```sql
-- Fiscal calendar reporting (July fiscal year start — AU/US federal)
SELECT
  DT_FISCAL_YEAR(sale_date, 7)       AS fy,
  DT_FISCAL_QUARTER(sale_date, 7)    AS fq,
  SUM(amount)                         AS revenue
FROM sales
GROUP BY 1, 2;

-- SLA tracking in business days
SELECT
  ticket_id,
  DT_BIZDAYS_BETWEEN(created_date, resolved_date) AS biz_days_to_resolve,
  DT_ADD_BIZDAYS(created_date, 5)                 AS sla_due_date
FROM support_tickets;

-- Age-based customer segmentation
SELECT customer_id,
  DT_AGE_YEARS(date_of_birth, CURRENT_DATE) AS age
FROM customers;

-- Millisecond-precision epoch (Kafka, JavaScript, event streams)
SELECT DT_TO_UNIX_MILLIS(event_timestamp) AS epoch_ms FROM event_log;

-- DATE_FORMAT (missing from Dremio natively)
SELECT DT_FORMAT(order_date, '%B %d, %Y') FROM orders;
-- → 'March 15, 2024'
```

| Category | Functions |
|---|---|
| Fiscal Calendar | `DT_FISCAL_YEAR`, `DT_FISCAL_QUARTER`, `DT_FISCAL_MONTH`, `DT_FISCAL_WEEK`, `DT_FISCAL_YEAR_START`, `DT_FISCAL_YEAR_END`, `DT_FISCAL_QUARTER_START`, `DT_FISCAL_QUARTER_END` |
| Business Days | `DT_IS_WEEKDAY`, `DT_BIZDAYS_BETWEEN`, `DT_ADD_BIZDAYS`, `DT_NEXT_WEEKDAY`, `DT_PREV_WEEKDAY` |
| Period End Boundaries | `DT_WEEK_END`, `DT_QUARTER_END`, `DT_YEAR_END`, `DT_DAYS_IN_MONTH` |
| Date Arithmetic | `DT_DIFF_MONTHS`, `DT_DIFF_YEARS`, `DT_AGE_YEARS`, `DT_IS_LEAP_YEAR` |
| Unix Epoch (Millis) | `DT_TO_UNIX_MILLIS`, `DT_FROM_UNIX_MILLIS`, `DT_TO_UNIX_SECONDS`, `DT_FROM_UNIX_SECONDS` |
| Formatting | `DT_FORMAT`, `DT_FORMAT_TS` |

**Key features:** 24 scalar UDFs · pure Java 11 (no external deps) · configurable fiscal year start month · weekday-only business day math · period end boundaries (week/quarter/year) · millisecond epoch conversions · strftime-style date formatting

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
# Geospatial UDF (shaded JAR with JTS + H3)
cd geo-udf
docker run --rm -v "$(pwd)":/project -v ~/.m2:/root/.m2 -w /project \
  maven:3.9-eclipse-temurin-11 \
  mvn package -DskipTests -Dcheckstyle.skip=true -Ddetekt.skip=true -Dmaven.javadoc.skip=true

# Vector UDF (no external deps)
cd vector-udf
docker run --rm -v "$(pwd)":/project -v ~/.m2:/root/.m2 -w /project \
  maven:3.9-eclipse-temurin-11 \
  mvn package -DskipTests

# ML UDF (no external deps)
cd ml-udf
docker run --rm -v "$(pwd)":/project -v ~/.m2:/root/.m2 -w /project \
  maven:3.9-eclipse-temurin-11 \
  mvn package -DskipTests

# PII UDF (no external deps)
cd pii-udf
docker run --rm -v "$(pwd)":/project -v ~/.m2:/root/.m2 -w /project \
  maven:3.9-eclipse-temurin-11 \
  mvn package -DskipTests

# Text Similarity UDF (no external deps)
cd text-similarity-udf
docker run --rm -v "$(pwd)":/project -v ~/.m2:/root/.m2 -w /project \
  maven:3.9-eclipse-temurin-11 \
  mvn package -DskipTests

# Date/Time UDF (no external deps)
cd datetime-udf
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
│   └── pom.xml
├── vector-udf/      — Vector UDF library (26 functions)
│   ├── jars/        — Pre-built JAR
│   ├── src/         — Java source
│   └── pom.xml
├── ml-udf/          — ML UDF library (32 functions)
│   ├── jars/        — Pre-built JAR
│   ├── src/         — Java source
│   └── pom.xml
├── pii-udf/              — PII UDF library (44 functions)
│   ├── jars/             — Pre-built JAR
│   ├── src/              — Java source (PiiUtils + 4 function files)
│   └── pom.xml
├── text-similarity-udf/  — Text Similarity UDF library (20 functions)
│   ├── jars/             — Pre-built JAR
│   ├── src/              — Java source (TextSimilarityUtils + 3 function files)
│   ├── install.sh
│   ├── rebuild.sh
│   └── pom.xml
├── datetime-udf/         — Date/Time UDF library (24 functions)
│   ├── jars/             — Pre-built JAR
│   ├── src/              — Java source (DateTimeUtils + 6 function files)
│   ├── install.sh
│   ├── rebuild.sh
│   └── pom.xml
└── .github/
    └── workflows/   — CI per library
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for how to report bugs, request features, and submit pull requests.

---

## License

Apache License 2.0

*Built by the Dremio Community*
