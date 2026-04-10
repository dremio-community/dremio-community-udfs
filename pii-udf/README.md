# Dremio PII UDF Library

**44 scalar UDFs** for PII (Personally Identifiable Information) detection, masking, extraction, and tokenization — directly in Dremio SQL. Covers email, phone, SSN, credit card, IBAN, IPv4/v6, ZIP, date of birth, passport, VIN, NPI, EIN, MAC address, and URL. Pure Java — no external dependencies.

> PII governance, data classification, and masking without leaving your lakehouse.

---

## Available Functions

### Detection Functions
All return `INT`: 1 = detected, 0 = not detected (never NULL — safe in WHERE clauses).

| Function | Detects | Validation |
|---|---|---|
| `IS_EMAIL(value)` | Email address | RFC-5321 pattern |
| `IS_PHONE(value)` | Phone number | NANP + international |
| `IS_SSN(value)` | US Social Security Number | Format + range validation |
| `IS_CREDIT_CARD(value)` | Credit card number | Pattern + Luhn checksum |
| `IS_IBAN(value)` | International Bank Account Number | Format + mod-97 checksum |
| `IS_IPV4(value)` | IPv4 address | Octet range (0–255) |
| `IS_IPV6(value)` | IPv6 address | Full + compressed forms |
| `IS_ZIP(value)` | US ZIP code | 5-digit or ZIP+4 |
| `IS_DOB(value)` | Date of birth | ISO (YYYY-MM-DD) + US (MM/DD/YYYY) + calendar |
| `IS_PASSPORT(value)` | Passport number | US 9-char + generic 6–9 char |
| `IS_VIN(value)` | Vehicle Identification Number | 17 chars + check digit |
| `IS_NPI(value)` | National Provider Identifier | 10 digits + Luhn variant |
| `IS_EIN(value)` | Employer Identification Number | XX-XXXXXXX + valid prefix |
| `IS_MAC(value)` | MAC address | Colon/hyphen/dot/bare formats |
| `IS_URL(value)` | HTTP/HTTPS URL | Full URL pattern |
| `IS_PII(value)` | Any of the above PII types | OR of all detectors |

### Classification Functions

| Function | Returns | Description |
|---|---|---|
| `PII_TYPE(value)` | VARCHAR | First detected PII type name (e.g. `'EMAIL'`, `'SSN'`), or NULL |
| `PII_SCORE(value)` | INT | Count of distinct PII types matched (0 = no PII) |

**PII_TYPE labels:** `EMAIL`, `SSN`, `CREDIT_CARD`, `IBAN`, `PHONE`, `IPV4`, `IPV6`, `US_ZIP`, `EIN`, `NPI`, `VIN`, `MAC_ADDRESS`, `PASSPORT`, `DATE_OF_BIRTH`, `URL`

### Masking Functions
Preserve format while hiding sensitive content — great for audit logs and de-identified datasets.

| Function | Description | Example |
|---|---|---|
| `MASK_EMAIL(email)` | Show first char + domain | `u***@example.com` |
| `MASK_EMAIL(email, char)` | Custom mask character | `u###@example.com` |
| `MASK_PHONE(phone)` | Show last 4 digits | `***-***-4567` |
| `MASK_SSN(ssn)` | Show last 4 digits | `***-**-6789` |
| `MASK_CREDIT_CARD(cc)` | Show last 4 digits | `****-****-****-1234` |
| `MASK_IBAN(iban)` | Show country code + last 4 | `GB82**************5432` |
| `MASK_IPV4(ip)` | Mask last 2 octets | `192.168.*.*` |
| `MASK_NAME(name)` | Show first letter of each word | `J*** S****` |
| `MASK_DOB(dob)` | Mask year | `****-06-15` |
| `MASK_CUSTOM(value, showLeft, showRight)` | Configurable reveal window | `He******rld` |
| `MASK_CUSTOM(value, showLeft, showRight, char)` | Custom mask character | `He######rld` |
| `PII_REDACT(value)` | Full redaction | `[REDACTED]` |

### Extraction Functions
Find and extract the first PII occurrence from free-form text.

| Function | Returns | Description |
|---|---|---|
| `EXTRACT_EMAIL(text)` | VARCHAR | First email address found in text |
| `EXTRACT_PHONE(text)` | VARCHAR | First phone number found in text |
| `EXTRACT_SSN(text)` | VARCHAR | First valid SSN found in text |
| `EXTRACT_CREDIT_CARD(text)` | VARCHAR | First Luhn-valid credit card found |
| `EXTRACT_IBAN(text)` | VARCHAR | First valid IBAN found |
| `EXTRACT_IPV4(text)` | VARCHAR | First IPv4 address found |
| `EXTRACT_IPV6(text)` | VARCHAR | First IPv6 address found |
| `EXTRACT_URL(text)` | VARCHAR | First HTTP/HTTPS URL found |
| `EXTRACT_ALL_PII(text)` | VARCHAR (JSON) | All detected PII as a JSON array |

`EXTRACT_ALL_PII` returns a JSON array of `{"type":"...", "value":"..."}` objects, or NULL if no PII is found.

### Hash / Tokenization Functions
Irreversibly transform PII for pseudonymization and referential integrity across de-identified datasets.

| Function | Returns | Description |
|---|---|---|
| `PII_SHA256(value)` | VARCHAR (64 hex) | SHA-256 hash of value |
| `PII_SHA256(value, salt)` | VARCHAR (64 hex) | Salted SHA-256: hash(salt + value) |
| `PII_MD5(value)` | VARCHAR (32 hex) | MD5 hash of value |
| `PII_TOKENIZE(value)` | VARCHAR | Stable token: `TOK-<first 16 hex chars of SHA-256>` |

---

## Quick Start

### Classify a Column for PII

```sql
SELECT
    column_name,
    IS_PII(column_value)    AS contains_pii,
    PII_TYPE(column_value)  AS pii_type,
    PII_SCORE(column_value) AS pii_score
FROM my_table;
```

### Mask PII Before Sharing

```sql
-- De-identify a customer table for analytics
SELECT
    customer_id,
    MASK_EMAIL(email)                   AS email_masked,
    MASK_PHONE(phone)                   AS phone_masked,
    MASK_NAME(full_name)                AS name_masked,
    MASK_CREDIT_CARD(card_number)       AS card_masked,
    MASK_DOB(date_of_birth)             AS dob_masked
FROM customers;
```

### Tokenize for Cross-System Joins

```sql
-- Create stable pseudonymous IDs — same email always maps to same token
SELECT
    PII_TOKENIZE(email) AS customer_token,
    SUM(purchase_amount) AS total_spend
FROM transactions
GROUP BY customer_token;
```

### Audit Log Scanning — Detect PII in Free Text

```sql
-- Find log records that contain PII
SELECT
    log_id,
    log_message,
    EXTRACT_EMAIL(log_message)  AS found_email,
    EXTRACT_IPV4(log_message)   AS found_ip,
    EXTRACT_URL(log_message)    AS found_url,
    EXTRACT_ALL_PII(log_message) AS all_pii_json
FROM application_logs
WHERE IS_PII(log_message) = 1
   OR EXTRACT_EMAIL(log_message) IS NOT NULL;
```

### Data Quality — Validate Identifiers

```sql
-- Flag customers with invalid credit cards, SSNs, or IBANs
SELECT
    customer_id,
    card_number,
    IS_CREDIT_CARD(card_number) AS card_valid,
    ssn,
    IS_SSN(ssn)                 AS ssn_valid,
    iban,
    IS_IBAN(iban)               AS iban_valid
FROM customers
WHERE IS_CREDIT_CARD(card_number) = 0
   OR IS_SSN(ssn) = 0
   OR IS_IBAN(iban) = 0;
```

### GDPR / CCPA — Find All PII in a Dataset

```sql
-- Data discovery: which columns contain which PII types?
SELECT
    'email_col'    AS column_name, COUNT(*) FILTER (WHERE IS_EMAIL(email_col) = 1)       AS pii_rows FROM customers
UNION ALL
SELECT 'phone_col',                COUNT(*) FILTER (WHERE IS_PHONE(phone_col) = 1)       FROM customers
UNION ALL
SELECT 'notes_col',                COUNT(*) FILTER (WHERE IS_PII(notes_col) = 1)         FROM customers;
```

### Salted Hashing for Multi-tenant Pseudonymization

```sql
-- Different tenants get different tokens for the same email
SELECT
    tenant_id,
    email,
    PII_SHA256(email, tenant_id) AS tenant_scoped_hash
FROM user_data;
```

---

## Validation Details

| Type | Algorithm |
|---|---|
| Credit Card | Luhn checksum (mod-10) |
| NPI | Luhn variant: prepend `80840`, run Luhn on 15-char string |
| IBAN | Mod-97 checksum (iterative, no BigInteger) |
| SSN | Regex + invalid range exclusion (000, 666, 900–999, group 00, serial 0000) |
| VIN | 17-char transliteration table + position-weighted check digit |
| EIN | Format + IRS campus prefix validation |
| Date of Birth | Calendar validation (leap year aware) |
| IPv4 | Per-octet range validation (0–255) |

---

## Installation

```bash
cp jars/dremio-pii-udf-1.0.0.jar /opt/dremio/jars/3rdparty/
# restart Dremio
```

**Docker:**
```bash
docker cp jars/dremio-pii-udf-1.0.0.jar try-dremio:/opt/dremio/jars/3rdparty/
docker restart try-dremio
```

---

## Building from Source

Requires Java 11+ and Maven (available inside Dremio's Docker container):

```bash
docker cp pii-udf/ try-dremio:/opt/dremio/data/pii-udf-build/
docker exec -u root try-dremio chown -R dremio:dremio /opt/dremio/data/pii-udf-build
docker exec try-dremio bash -c "mkdir -p /opt/dremio/data/pii-udf-build/jars && cd /opt/dremio/data/pii-udf-build && mvn package -q -DskipTests"
docker cp try-dremio:/opt/dremio/data/pii-udf-build/jars/dremio-pii-udf-1.0.0.jar jars/
```

---

## Test Results

All 39 smoke tests pass against a live Dremio 26.x instance:

```
✅ IS_EMAIL('user@example.com')              → 1
✅ IS_EMAIL('notanemail')                    → 0
✅ IS_SSN('123-45-6789')                     → 1
✅ IS_SSN('000-45-6789')                     → 0  (invalid area)
✅ IS_CREDIT_CARD('4532015112830366')         → 1  (Luhn valid Visa)
✅ IS_CREDIT_CARD('1234567890123456')         → 0  (Luhn fail)
✅ IS_IBAN('GB82WEST12345698765432')          → 1  (mod-97 valid)
✅ IS_IPV4('192.168.1.1')                    → 1
✅ IS_IPV4('999.168.1.1')                    → 0  (invalid octet)
✅ IS_IPV6('2001:0db8:85a3::8a2e:0370:7334') → 1
✅ IS_ZIP('90210')                           → 1
✅ IS_ZIP('9021')                            → 0  (too short)
✅ IS_VIN('1HGBH41JXMN109186')              → 1  (check digit valid)
✅ IS_NPI('1234567893')                      → 1  (Luhn variant valid)
✅ IS_EIN('12-3456789')                      → 1  (valid prefix)
✅ IS_MAC('00:1A:2B:3C:4D:5E')              → 1
✅ IS_URL('https://www.example.com')         → 1
✅ IS_PII('user@example.com')               → 1
✅ IS_PII('hello world')                    → 0
✅ PII_TYPE('user@example.com')             → 'EMAIL'
✅ PII_TYPE('123-45-6789')                  → 'SSN'
✅ PII_TYPE('192.168.1.1')                  → 'IPV4'
✅ PII_SCORE('user@example.com')            → 1
✅ MASK_EMAIL('user@example.com')           → 'u***@example.com'
✅ MASK_PHONE('5551234567')                 → '***-***-4567'
✅ MASK_SSN('123-45-6789')                  → '***-**-6789'
✅ MASK_CREDIT_CARD('4532015112830366')      → '****-****-****-0366'
✅ MASK_IBAN('GB82WEST12345698765432')       → 'GB82**************5432'
✅ MASK_IPV4('192.168.1.100')               → '192.168.*.*'
✅ MASK_NAME('John Smith')                  → 'J*** S****'
✅ MASK_DOB('1985-06-15')                   → '****-06-15'
✅ MASK_CUSTOM('Hello World', 2, 3)         → 'He******rld'
✅ PII_REDACT('John Smith')                 → '[REDACTED]'
✅ EXTRACT_EMAIL('Contact: user@example.com for info') → 'user@example.com'
✅ EXTRACT_IPV4('Server at 192.168.1.1 is down')       → '192.168.1.1'
✅ EXTRACT_URL('Visit https://example.com today')      → 'https://example.com'
✅ LENGTH(PII_SHA256('test'))               → 64
✅ LENGTH(PII_MD5('test'))                  → 32
✅ LEFT(PII_TOKENIZE('test@example.com'), 4) → 'TOK-'
Results: 39/39 passed ✅
```
