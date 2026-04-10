# Dremio ML UDF Library

Scalar UDFs that bring classical machine learning operations directly into Dremio SQL. Score models, engineer features, detect anomalies, and evaluate predictions — all without leaving your lakehouse.

> **No external dependencies.** Pure Java, zero-overhead. Train your models in Python/sklearn/XGBoost; deploy scoring and feature logic as SQL.

## Available Functions

### Activation Functions
| Function | Description | Output Range |
|---|---|---|
| `ML_SIGMOID(x)` | Logistic sigmoid: 1 / (1 + exp(−x)) | (0, 1) |
| `ML_RELU(x)` | Rectified Linear Unit: max(0, x) | [0, ∞) |
| `ML_LEAKY_RELU(x, alpha)` | Leaky ReLU: x if x > 0, else alpha × x | (−∞, ∞) |
| `ML_ELU(x, alpha)` | Exponential Linear Unit | (−alpha, ∞) |
| `ML_SWISH(x)` | Swish: x × sigmoid(x) | (−∞, ∞) |
| `ML_GELU(x)` | Gaussian Error Linear Unit (fast approx) | (−∞, ∞) |

### Model Scoring / Inference
Vectors are passed as `VARCHAR` JSON-array strings: `'[0.1, 0.2, 0.3]'`

| Function | Description | Returns |
|---|---|---|
| `ML_LINEAR_SCORE(features, weights, bias)` | Linear regression score: dot(f, w) + bias | DOUBLE |
| `ML_LOGISTIC_SCORE(features, weights, bias)` | Logistic regression probability: sigmoid(linear_score) | DOUBLE (0–1) |
| `ML_SOFTMAX_SCORE(scores)` | Softmax over a logit vector → probability distribution | VARCHAR (JSON array) |
| `ML_ARGMAX(scores)` | Index of the maximum value in a score vector | INT |
| `ML_ARGMIN(scores)` | Index of the minimum value in a score vector | INT |

### Feature Engineering
| Function | Description | Returns |
|---|---|---|
| `ML_ZSCORE(value, mean, stddev)` | Standard score: (x − μ) / σ | DOUBLE |
| `ML_MIN_MAX_SCALE(value, min, max)` | Scale to [0, 1]: (x − min) / (max − min) | DOUBLE |
| `ML_MIN_MAX_SCALE_RANGE(value, min, max, a, b)` | Scale to custom range [a, b] | DOUBLE |
| `ML_ROBUST_SCALE(value, median, iqr)` | Outlier-resistant scaling: (x − median) / IQR | DOUBLE |
| `ML_BINARIZE(value, threshold)` | 1 if value ≥ threshold, else 0 | INT |
| `ML_BIN(value, edges)` | Bucket index given a JSON edge array | INT |
| `ML_CLIP_VALUE(value, min, max)` | Clamp scalar to [min, max] | DOUBLE |
| `ML_LOG1P(value)` | Log-transform: log(1 + x) — for skewed data | DOUBLE |

### Categorical Encoding
`categories` is a JSON string array: `'["cat","dog","bird"]'`

| Function | Description | Returns |
|---|---|---|
| `ML_LABEL_ENCODE(value, categories)` | 0-based index in category list; −1 if not found | INT |
| `ML_ONE_HOT_ENCODE(value, categories)` | Binary vector with 1 at matching index | VARCHAR (JSON int array) |
| `ML_HASH_ENCODE(value, num_buckets)` | Stable hash bucket in [0, num_buckets) | INT |

### Clustering / Segment Assignment
`centroids` is a JSON array-of-arrays: `'[[1.0,2.0],[3.0,4.0]]'`

| Function | Description | Returns |
|---|---|---|
| `ML_KMEANS_ASSIGN(point, centroids)` | Index of the nearest centroid (Euclidean) | INT |
| `ML_KMEANS_DISTANCE(point, centroids)` | Euclidean distance to the nearest centroid | DOUBLE |

### Anomaly Detection
| Function | Description | Returns |
|---|---|---|
| `ML_IQR_OUTLIER(value, q1, q3, multiplier)` | 1 if outside Tukey fence (Q1−k·IQR, Q3+k·IQR) | INT (1/0) |
| `ML_ZSCORE_OUTLIER(value, mean, stddev, threshold)` | 1 if \|z-score\| > threshold | INT (1/0) |
| `ML_WINSORIZE(value, lower, upper)` | Clip to [lower, upper] bounds | DOUBLE |
| `ML_ISOLATION_SCORE(value, mean, stddev)` | Gaussian anomaly probability in (0, 1) | DOUBLE |

### Evaluation Metrics
These are **per-row component functions** — wrap with `AVG()` to get aggregate metrics.

| Function | Description | Returns | Aggregate |
|---|---|---|---|
| `ML_SQUARED_ERROR(actual, predicted)` | (y − ŷ)² | DOUBLE | `AVG()` → MSE |
| `ML_ABS_ERROR(actual, predicted)` | \|y − ŷ\| | DOUBLE | `AVG()` → MAE |
| `ML_LOG_LOSS(actual, predicted_prob)` | Binary cross-entropy component | DOUBLE | `AVG()` → log-loss |
| `ML_HUBER_LOSS(actual, predicted, delta)` | Huber loss (robust to outliers) | DOUBLE | `AVG()` → Huber |
| `ML_ACCURACY_HIT(actual, predicted)` | 1 if equal, else 0 | INT | `AVG()` → accuracy |

---

## Quick Start

### Logistic Regression Scoring
Train your model in Python, export the weights, score every row in SQL:

```sql
-- Churn prediction: score every customer
SELECT
    customer_id,
    ML_LOGISTIC_SCORE(
        feature_vector,                         -- pre-built JSON feature array
        '[0.42, -0.18, 0.91, 0.05, -0.33]',    -- model weights
        CAST(-0.72 AS DOUBLE)                   -- bias term
    ) AS churn_probability
FROM customer_features
ORDER BY churn_probability DESC
LIMIT 100;
```

### Feature Engineering Pipeline
```sql
SELECT
    customer_id,
    ML_ZSCORE(age,       35.4, 12.1)          AS age_scaled,
    ML_ZSCORE(income,    62000.0, 28000.0)    AS income_scaled,
    ML_MIN_MAX_SCALE(credit_score, 300, 850)  AS credit_scaled,
    ML_LOG1P(num_transactions)                AS txn_log,
    ML_BINARIZE(has_churned, 0.5)             AS label,
    ML_BIN(credit_score, '[580,670,740,800]') AS credit_tier
FROM customers;
```

### Softmax Multi-Class Classification
```sql
-- Predict the most likely product category from a score vector
SELECT
    product_id,
    ML_ARGMAX(ML_SOFTMAX_SCORE(raw_logits))  AS predicted_category,
    ML_SOFTMAX_SCORE(raw_logits)             AS class_probabilities
FROM product_classifier_output;
```

### K-Means Customer Segmentation
Export your trained centroids from sklearn as JSON, then assign segments in SQL:

```sql
-- Assign every customer to one of 5 segments
SELECT
    customer_id,
    ML_KMEANS_ASSIGN(
        feature_vector,
        '[[0.1,0.8,0.2],[0.9,0.1,0.7],[0.5,0.5,0.5],[0.2,0.3,0.9],[0.8,0.6,0.1]]'
    ) AS segment,
    ML_KMEANS_DISTANCE(
        feature_vector,
        '[[0.1,0.8,0.2],[0.9,0.1,0.7],[0.5,0.5,0.5],[0.2,0.3,0.9],[0.8,0.6,0.1]]'
    ) AS distance_to_centroid
FROM customer_embeddings;
```

### Anomaly Detection on Transactions
```sql
-- Flag suspicious transactions using both IQR and z-score methods
SELECT
    transaction_id,
    amount,
    ML_IQR_OUTLIER(amount, 45.0, 210.0, 1.5)          AS iqr_flag,
    ML_ZSCORE_OUTLIER(amount, 127.5, 88.3, 3.0)        AS zscore_flag,
    ML_WINSORIZE(amount, 5.0, 1000.0)                  AS amount_capped,
    ML_ISOLATION_SCORE(amount, 127.5, 88.3)             AS anomaly_prob
FROM transactions
WHERE ML_IQR_OUTLIER(amount, 45.0, 210.0, 1.5) = 1
   OR ML_ZSCORE_OUTLIER(amount, 127.5, 88.3, 3.0) = 1;
```

### Model Evaluation
```sql
-- Evaluate a regression model: MSE, RMSE, MAE in one query
SELECT
    AVG(ML_SQUARED_ERROR(actual_price, predicted_price))              AS mse,
    SQRT(AVG(ML_SQUARED_ERROR(actual_price, predicted_price)))        AS rmse,
    AVG(ML_ABS_ERROR(actual_price, predicted_price))                  AS mae
FROM predictions;

-- Evaluate a classifier: accuracy and log-loss
SELECT
    AVG(ML_ACCURACY_HIT(actual_label, predicted_label))               AS accuracy,
    AVG(ML_LOG_LOSS(CAST(actual_label AS DOUBLE), predicted_prob))    AS log_loss
FROM classifier_results;
```

### Categorical Encoding
```sql
SELECT
    product_id,
    ML_LABEL_ENCODE(category, '["electronics","clothing","food","home"]')   AS category_id,
    ML_ONE_HOT_ENCODE(color, '["red","green","blue","black","white"]')       AS color_vec,
    ML_HASH_ENCODE(user_agent, 256)                                          AS agent_bucket
FROM events;
```

---

## Input Format

| Type | Format | Example |
|---|---|---|
| Scalar numeric | `DOUBLE` / `FLOAT` literal or column | `0.5`, `CAST(col AS DOUBLE)` |
| Vector | `VARCHAR` JSON array | `'[0.1, 0.2, -0.3]'` |
| Category list | `VARCHAR` JSON string array | `'["cat","dog","bird"]'` |
| Centroid matrix | `VARCHAR` JSON array-of-arrays | `'[[1.0,2.0],[3.0,4.0]]'` |
| Bin edges | `VARCHAR` JSON numeric array | `'[300,580,670,740,850]'` |

---

## Installation

Copy the JAR to Dremio's third-party plugin directory and restart:

```bash
cp jars/dremio-ml-udf-1.0.0.jar /opt/dremio/jars/3rdparty/
# restart Dremio
```

**Docker:**
```bash
docker cp jars/dremio-ml-udf-1.0.0.jar try-dremio:/opt/dremio/jars/3rdparty/
docker restart try-dremio
```

---

## Building from Source

Requires Java 11+ and Maven (or the included `./mvnw` wrapper).

```bash
# Build inside a running Dremio Docker container (recommended)
docker cp . try-dremio:/opt/dremio/data/ml-udf-build
docker exec try-dremio bash -c "cd /opt/dremio/data/ml-udf-build && mvn clean package -q"
docker cp try-dremio:/opt/dremio/data/ml-udf-build/jars/dremio-ml-udf-1.0.0.jar jars/
```

---

## Test Results

All 40 smoke tests pass against a live Dremio 26.x instance:

```
✅ ML_SIGMOID(0)                          → 0.5
✅ ML_SIGMOID(2)                          → 0.8808
✅ ML_RELU(-1)                            → 0.0
✅ ML_RELU(3)                             → 3.0
✅ ML_LEAKY_RELU(-2, 0.1)                 → -0.2
✅ ML_ELU(-1, 1)                          → -0.6321
✅ ML_SWISH(1)                            → 0.7311
✅ ML_GELU(1)                             → 0.8412
✅ ML_LINEAR_SCORE([1,2],[0.5,0.5],1)     → 2.5
✅ ML_LOGISTIC_SCORE([1,2],[0.5,0.5],0)   → 0.8176
✅ ML_SOFTMAX_SCORE([1,2,3])              → [0.090,0.245,0.665]
✅ ML_ARGMAX([1,5,3,2])                   → 1
✅ ML_ARGMIN([1,5,0,2])                   → 2
✅ ML_ZSCORE(75, 50, 10)                  → 2.5
✅ ML_MIN_MAX_SCALE(75, 50, 100)          → 0.5
✅ ML_ROBUST_SCALE(75, 60, 20)            → 0.75
✅ ML_BINARIZE(0.6, 0.5)                  → 1
✅ ML_BIN(75, [50,70,90])                 → 2
✅ ML_CLIP_VALUE(150, 0, 100)             → 100.0
✅ ML_LOG1P(9)                            → 2.303
✅ ML_LABEL_ENCODE('dog', [...])          → 1
✅ ML_ONE_HOT_ENCODE('dog', [...])        → [0,1,0]
✅ ML_HASH_ENCODE('hello', 128)           → 82
✅ ML_KMEANS_ASSIGN([0.1,0.1], ...)       → 0
✅ ML_KMEANS_ASSIGN([9.9,9.9], ...)       → 2
✅ ML_KMEANS_DISTANCE([0.1,0.1], ...)     → 0.1414
✅ ML_IQR_OUTLIER(200, 45, 90, 1.5)      → 1  (outlier)
✅ ML_ZSCORE_OUTLIER(100, 50, 10, 3.0)   → 1  (outlier)
✅ ML_WINSORIZE(250, 0, 100)              → 100.0
✅ ML_ISOLATION_SCORE(50, 50, 10)         → 0.0  (not anomalous)
✅ ML_SQUARED_ERROR(3, 2)                 → 1.0
✅ ML_ABS_ERROR(3, 2)                     → 1.0
✅ ML_LOG_LOSS(1, 0.9)                    → 0.1054
✅ ML_HUBER_LOSS(3, 2, 1.0)              → 0.5
✅ ML_ACCURACY_HIT(1, 1)                  → 1
✅ ML_ACCURACY_HIT(1, 0)                  → 0
Results: 40/40 passed ✅
```
