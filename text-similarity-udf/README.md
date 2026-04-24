# Dremio Text Similarity UDF Library

*Built by Mark Shainman*

20 scalar UDFs for fuzzy string matching, text similarity, and string distance in Dremio SQL. No external runtime dependencies — pure Java 11.

Dremio has `SOUNDEX`, `REGEXP_MATCHES`, and `SIMILAR TO` natively. This library adds the algorithms those don't cover: edit distance, Jaro-Winkler, n-gram similarity, word-set measures, and token-based fuzzy matching.

---

## Quick Install

```bash
# Docker — use the pre-built JAR (no Maven needed)
./install.sh --docker try-dremio --prebuilt

# Bare-metal Dremio
./install.sh --local /opt/dremio --prebuilt

# Kubernetes pod
./install.sh --k8s dremio-0 --prebuilt
```

After restart, all `TEXT_*` functions are available in SQL immediately.

---

## Functions

### Edit Distance

| Function | Returns | Description |
|---|---|---|
| `TEXT_LEVENSHTEIN(s1, s2)` | INT | Minimum single-character edits (insert/delete/substitute) |
| `TEXT_LEVENSHTEIN_SIMILARITY(s1, s2)` | FLOAT | Normalized edit similarity: `1 - distance/max_length` (0–1) |
| `TEXT_HAMMING_DISTANCE(s1, s2)` | INT | Positions where characters differ. Returns -1 if lengths differ. |
| `TEXT_LCS_LENGTH(s1, s2)` | INT | Length of the longest common subsequence |
| `TEXT_PARTIAL_RATIO(s1, s2)` | FLOAT | Best Levenshtein similarity of shorter string against any window of longer |

### Phonetic / Character Similarity

| Function | Returns | Description |
|---|---|---|
| `TEXT_JARO(s1, s2)` | FLOAT | Jaro similarity (0–1) |
| `TEXT_JARO_WINKLER(s1, s2)` | FLOAT | Jaro-Winkler — boosts common prefixes. Best for names. |
| `TEXT_TRIGRAM_SIMILARITY(s1, s2)` | FLOAT | 3-gram character Jaccard similarity |
| `TEXT_BIGRAM_SIMILARITY(s1, s2)` | FLOAT | 2-gram character Jaccard similarity |
| `TEXT_NGRAM_SIMILARITY(s1, s2, n)` | FLOAT | n-gram character Jaccard similarity (configurable n) |

### Word-Set Similarity

| Function | Returns | Description |
|---|---|---|
| `TEXT_JACCARD_SIMILARITY(s1, s2)` | FLOAT | `\|A∩B\| / \|A∪B\|` on word sets |
| `TEXT_DICE_SIMILARITY(s1, s2)` | FLOAT | `2\|A∩B\| / (\|A\|+\|B\|)` — Sørensen–Dice |
| `TEXT_OVERLAP_COEFFICIENT(s1, s2)` | FLOAT | `\|A∩B\| / min(\|A\|,\|B\|)` — 1.0 when smaller set ⊆ larger |
| `TEXT_COSINE_SIMILARITY(s1, s2)` | FLOAT | TF cosine similarity on word frequency vectors |

### Token-Based Fuzzy Matching

| Function | Returns | Description |
|---|---|---|
| `TEXT_TOKEN_SORT_RATIO(s1, s2)` | FLOAT | Sort tokens alphabetically, then compare — handles word-order differences |
| `TEXT_TOKEN_SET_RATIO(s1, s2)` | FLOAT | Set intersection + diff comparison — handles subset/superset strings |
| `TEXT_FUZZY_MATCH(s1, s2, threshold)` | BIT | 1 if Jaro-Winkler ≥ threshold. Use 0.85–0.92 for names. |
| `TEXT_IS_SIMILAR(s1, s2, threshold)` | BIT | 1 if trigram similarity ≥ threshold. Better for multi-word strings. |

### Normalization

| Function | Returns | Description |
|---|---|---|
| `TEXT_NORMALIZE(s)` | VARCHAR | Lowercase + remove punctuation + collapse whitespace |
| `TEXT_REMOVE_DIACRITICS(s)` | VARCHAR | Strip accents: `é→e`, `ñ→n`, `ü→u`, `résumé→resume` |

---

## SQL Examples

```sql
-- Basic edit distance
SELECT TEXT_LEVENSHTEIN('kitten', 'sitting');         -- 3
SELECT TEXT_LEVENSHTEIN_SIMILARITY('kitten', 'sitting'); -- 0.571

-- Name matching (best for single words / names)
SELECT TEXT_JARO_WINKLER('Robert', 'Rupert');         -- 0.933
SELECT TEXT_JARO_WINKLER('Johnson', 'Johnston');      -- 0.971

-- Fuzzy join: Salesforce contacts to CRM records
SELECT s.Id, c.customer_id
FROM salesforce.Contact s
JOIN iceberg.crm.customers c
  ON TEXT_FUZZY_MATCH(s.Name, c.full_name, 0.88) = 1;

-- Multi-word fuzzy matching (handles word order and extra words)
SELECT TEXT_TOKEN_SORT_RATIO('John Michael Smith', 'Smith John Michael'); -- 1.0
SELECT TEXT_TOKEN_SET_RATIO('Quick Brown Fox', 'Brown Fox');              -- 1.0
SELECT TEXT_PARTIAL_RATIO('John', 'John Michael Smith');                   -- 1.0

-- Document / phrase similarity
SELECT TEXT_TRIGRAM_SIMILARITY('colour', 'color');    -- 0.4
SELECT TEXT_JACCARD_SIMILARITY('the cat sat', 'the cat mat'); -- 0.5
SELECT TEXT_COSINE_SIMILARITY('apple apple banana', 'apple banana'); -- 0.943

-- Boolean gates with thresholds
SELECT
  name,
  candidate,
  TEXT_JARO_WINKLER(name, candidate)           AS jw_score,
  TEXT_FUZZY_MATCH(name, candidate, 0.85)      AS is_match
FROM my_dedup_table;

-- Preprocessing before comparison
SELECT TEXT_LEVENSHTEIN(
  TEXT_NORMALIZE(TEXT_REMOVE_DIACRITICS(a)),
  TEXT_NORMALIZE(TEXT_REMOVE_DIACRITICS(b))
) AS normalized_distance
FROM my_table;

-- Entity resolution across sources
SELECT
  s.Id                                   AS sf_id,
  d.pk                                   AS dynamo_pk,
  TEXT_JARO_WINKLER(s.Name, d.name)      AS name_sim,
  TEXT_TRIGRAM_SIMILARITY(s.Email, d.email) AS email_sim
FROM salesforce.Contact s
CROSS JOIN dynamodb.customers d
WHERE TEXT_FUZZY_MATCH(s.Name, d.name, 0.88) = 1
  AND s.Email = d.email;
```

---

## Choosing the Right Function

| Use case | Recommended function |
|---|---|
| Single-word / name matching | `TEXT_JARO_WINKLER` |
| Multi-word strings with word-order variation | `TEXT_TOKEN_SORT_RATIO` |
| One string is a subset of the other | `TEXT_TOKEN_SET_RATIO` or `TEXT_OVERLAP_COEFFICIENT` |
| Address / phrase similarity | `TEXT_TRIGRAM_SIMILARITY` |
| Document / longer text | `TEXT_COSINE_SIMILARITY` or `TEXT_JACCARD_SIMILARITY` |
| Exact edit cost needed | `TEXT_LEVENSHTEIN` |
| Abbreviation / prefix matching | `TEXT_PARTIAL_RATIO` |
| Preprocessing multilingual data | `TEXT_REMOVE_DIACRITICS` + `TEXT_NORMALIZE` |
| Boolean filter in WHERE clause | `TEXT_FUZZY_MATCH` or `TEXT_IS_SIMILAR` |

---

## Upgrading Dremio

```bash
./rebuild.sh --docker try-dremio         # auto-detects version, rebuilds, redeploys
./rebuild.sh --docker try-dremio --force # force rebuild even if version matches
./rebuild.sh --dry-run                   # preview detected version only
```

---

## Requirements

- Dremio OSS 26.x
- Java 11+ (provided by Dremio container)
- Maven 3.8+ (only for source builds)

---

## Tests

84 unit tests covering all utility methods. Run without a Dremio instance:

```bash
# Inside the Dremio container (has Java + Maven)
docker exec try-dremio bash -c "cd /tmp/text-similarity-build && mvn test"
```

**Result:** 84/84 tests passing.
