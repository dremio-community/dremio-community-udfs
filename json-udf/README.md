# Dremio JSON UDF Library

21 scalar UDFs for parsing, extracting, inspecting, manipulating, and building JSON strings in Dremio SQL. Works on VARCHAR columns containing JSON — complements Dremio's native `CONVERT_FROM`/`FLATTEN` which operate on native nested types.

## Functions

### Extraction — pull typed values from JSON strings by path

Path syntax: dot-notation — `"a.b.c"` for nested keys, `"items.0.name"` for array indices.

| Function | Returns | Description |
|----------|---------|-------------|
| `JSON_EXTRACT_STR(json, path)` | VARCHAR | Extract a string value; returns raw JSON for objects/arrays |
| `JSON_EXTRACT_INT(json, path)` | BIGINT | Extract an integer value |
| `JSON_EXTRACT_FLOAT(json, path)` | FLOAT | Extract a floating-point value |
| `JSON_EXTRACT_BOOL(json, path)` | BIT | Extract a boolean (1=true, 0=false) |
| `JSON_EXTRACT_RAW(json, path)` | VARCHAR | Extract raw JSON at path (object/array preserved as string) |

All extraction functions return NULL if the path is missing or the type doesn't match.

### Inspection — examine JSON structure

| Function | Returns | Description |
|----------|---------|-------------|
| `JSON_IS_VALID(json)` | BIT | 1 if valid JSON, 0 otherwise |
| `JSON_TYPE(json)` | VARCHAR | `"object"`, `"array"`, `"string"`, `"number"`, `"boolean"`, `"null"`, or `"invalid"` |
| `JSON_LENGTH(json)` | BIGINT | Key count for objects, element count for arrays; -1 for other types |
| `JSON_HAS_KEY(json, key)` | BIT | 1 if top-level key exists in object |
| `JSON_KEYS(json)` | VARCHAR | Comma-separated list of top-level object keys; NULL for non-objects |

### Manipulation — modify JSON strings

| Function | Returns | Description |
|----------|---------|-------------|
| `JSON_SET(json, key, value)` | VARCHAR | Set top-level key; value parsed as JSON if valid, else stored as string |
| `JSON_DELETE(json, key)` | VARCHAR | Remove a top-level key |
| `JSON_MERGE(json1, json2)` | VARCHAR | Shallow merge; json2 keys overwrite json1 |
| `JSON_PRETTY(json)` | VARCHAR | Pretty-print with indentation |
| `JSON_MINIFY(json)` | VARCHAR | Compact JSON removing all whitespace |

### Array operations

| Function | Returns | Description |
|----------|---------|-------------|
| `JSON_ARRAY_LENGTH(json)` | BIGINT | Element count for arrays; -1 for non-arrays |
| `JSON_ARRAY_GET(json, index)` | VARCHAR | Element at 0-based index; NULL if out of bounds |
| `JSON_ARRAY_CONTAINS_STR(json, value)` | BIT | 1 if any element string-equals value |
| `JSON_ARRAY_APPEND(json, value)` | VARCHAR | Append element; value parsed as JSON if valid |

### Build — construct JSON from SQL values

| Function | Returns | Description |
|----------|---------|-------------|
| `JSON_FROM_KV(key, value)` | VARCHAR | Create `{"key":"value"}` from two strings |
| `JSON_WRAP_STR(value)` | VARCHAR | Wrap a string as a JSON string literal with proper escaping |

## Installation

**First-time install (prebuilt JAR — no Maven required):**

```bash
# Docker
./install.sh --docker try-dremio --prebuilt

# Bare-metal
./install.sh --local /opt/dremio --prebuilt

# Kubernetes
./install.sh --k8s dremio-0 --prebuilt
```

**After a Dremio upgrade (auto-detects version, rebuilds from source):**

```bash
./rebuild.sh --docker try-dremio
./rebuild.sh --local /opt/dremio
```

## Usage

```sql
-- Extract typed fields from a JSON event payload
SELECT
  JSON_EXTRACT_STR(payload, 'user.name')   AS name,
  JSON_EXTRACT_INT(payload, 'user.age')    AS age,
  JSON_EXTRACT_FLOAT(payload, 'score')     AS score,
  JSON_EXTRACT_BOOL(payload, 'active')     AS is_active
FROM events;

-- Filter on JSON field values
SELECT * FROM events
WHERE JSON_EXTRACT_BOOL(payload, 'active') = true
  AND JSON_EXTRACT_FLOAT(payload, 'score') > 8.0;

-- Validate and type-check incoming JSON
SELECT
  JSON_IS_VALID(raw_payload)   AS is_valid,
  JSON_TYPE(raw_payload)       AS json_type,
  JSON_LENGTH(raw_payload)     AS key_count,
  JSON_KEYS(raw_payload)       AS top_level_keys
FROM raw_events;

-- Check for required fields
SELECT *
FROM events
WHERE JSON_HAS_KEY(payload, 'user_id') = false;

-- Merge a default config with per-row overrides
SELECT JSON_MERGE('{"timeout":30,"retries":3}', config_override) AS final_config
FROM pipeline_runs;

-- Set / delete fields to normalize payloads
SELECT
  JSON_DELETE(
    JSON_SET(payload, 'processed_at', JSON_WRAP_STR(CURRENT_DATE)),
    'internal_debug'
  ) AS clean_payload
FROM raw_events;

-- Work with nested arrays
SELECT
  JSON_ARRAY_LENGTH(JSON_EXTRACT_RAW(payload, 'tags')) AS tag_count,
  JSON_ARRAY_GET(JSON_EXTRACT_RAW(payload, 'tags'), 0) AS first_tag,
  JSON_ARRAY_CONTAINS_STR(JSON_EXTRACT_RAW(payload, 'tags'), 'vip') AS is_vip
FROM events;

-- Build JSON from individual columns
SELECT JSON_FROM_KV('user_id', CAST(user_id AS VARCHAR)) AS id_json
FROM users;

-- Pretty-print for debugging
SELECT JSON_PRETTY(payload) FROM events LIMIT 1;
```

## Test Data

A sample table is included for testing:

```sql
SELECT * FROM iceberg_minio."dremio-test".json_test_events LIMIT 10;
```

The table has 10 rows with two JSON columns:
- `payload` — user profile: `{"name":..., "age":..., "email":..., "active":..., "score":..., "tags":[...]}`
- `event_meta` — event context: `{"source":..., "region":..., "campaign":...}`

## Notes

**Path syntax:** Uses dot-notation (`a.b.c`). Array indices are numeric segments: `items.0.name` gets the `name` field of the first element of `items`.

**vs. Dremio built-ins:** `CONVERT_FROM(col, 'json')` parses a JSON column into a native Dremio map/list for querying — best for JSON data sources. These UDFs operate on VARCHAR fields and are better suited for validation, manipulation, and extraction from JSON stored as strings in relational tables.

**Null handling:** All functions return NULL on null input. Extraction functions additionally return NULL when the path is missing or the type doesn't match.

## Tests

- **63 unit tests** in `JsonUdfTest.java` (`mvn test`)
- **23/23 live tests** verified against Dremio 26.0.5
