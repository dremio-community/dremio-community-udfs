# Dremio Crypto UDF Library

15 scalar UDFs for cryptographic hashing, HMAC authentication, AES encryption, and encoding — all available in Dremio SQL. Pure Java, no external dependencies.

## Functions

### Hashing

| Function | Returns | Description |
|----------|---------|-------------|
| `CRYPTO_MD5(input)` | VARCHAR | MD5 hash as lowercase hex (32 chars) |
| `CRYPTO_SHA1(input)` | VARCHAR | SHA-1 hash as lowercase hex (40 chars) |
| `CRYPTO_SHA256(input)` | VARCHAR | SHA-256 hash as lowercase hex (64 chars) |
| `CRYPTO_SHA512(input)` | VARCHAR | SHA-512 hash as lowercase hex (128 chars) |
| `CRYPTO_CRC32(input)` | BIGINT | CRC-32 checksum as unsigned integer |

### HMAC

| Function | Returns | Description |
|----------|---------|-------------|
| `CRYPTO_HMAC_SHA256(message, key)` | VARCHAR | HMAC-SHA256 as lowercase hex |
| `CRYPTO_HMAC_SHA512(message, key)` | VARCHAR | HMAC-SHA512 as lowercase hex |

### Encoding

| Function | Returns | Description |
|----------|---------|-------------|
| `CRYPTO_BASE64_ENCODE(input)` | VARCHAR | Base64-encode a string |
| `CRYPTO_BASE64_DECODE(input)` | VARCHAR | Decode a Base64 string |
| `CRYPTO_HEX_ENCODE(input)` | VARCHAR | Hex-encode a string (UTF-8 bytes → hex) |
| `CRYPTO_HEX_DECODE(input)` | VARCHAR | Decode a hex string back to text |

### Encryption

| Function | Returns | Description |
|----------|---------|-------------|
| `CRYPTO_AES_ENCRYPT(plaintext, key)` | VARCHAR | AES-256-CBC encrypt; returns `base64(iv + ciphertext)` |
| `CRYPTO_AES_DECRYPT(ciphertext, key)` | VARCHAR | AES-256-CBC decrypt; expects output of `CRYPTO_AES_ENCRYPT` |

### Utility

| Function | Returns | Description |
|----------|---------|-------------|
| `CRYPTO_RANDOM_UUID()` | VARCHAR | Generate a random UUID v4 |
| `CRYPTO_CONSTANT_TIME_EQUALS(a, b)` | BIT | Timing-safe string comparison (prevents timing attacks on hash comparisons) |

All functions return NULL if any input is NULL. On error, hash/encoding functions return `ERROR:<message>`.

## Installation

```bash
# Docker
docker cp jars/dremio-crypto-udf-1.0.0.jar try-dremio:/opt/dremio/jars/3rdparty/
docker restart try-dremio

# Bare-metal
cp jars/dremio-crypto-udf-1.0.0.jar /opt/dremio/jars/3rdparty/
# Restart Dremio
```

## Usage

```sql
-- Hash a password or sensitive value
SELECT CRYPTO_SHA256(email) AS email_hash FROM users;

-- Verify a stored hash without timing attack risk
SELECT CRYPTO_CONSTANT_TIME_EQUALS(
  CRYPTO_SHA256(submitted_token),
  stored_hash
) AS is_valid FROM api_keys;

-- HMAC for webhook signature verification
SELECT CRYPTO_HMAC_SHA256(payload, 'webhook-secret') AS signature;

-- AES round-trip encrypt/decrypt
SELECT CRYPTO_AES_DECRYPT(
  CRYPTO_AES_ENCRYPT('sensitive data', 'my-encryption-key'),
  'my-encryption-key'
) AS recovered;

-- Encode/decode
SELECT CRYPTO_BASE64_ENCODE('Hello, World!');   -- aGVsbG8sIFdvcmxkIQ==
SELECT CRYPTO_BASE64_DECODE('aGVsbG8=');        -- hello
SELECT CRYPTO_HEX_ENCODE('hello');              -- 68656c6c6f
SELECT CRYPTO_HEX_DECODE('68656c6c6f');         -- hello

-- CRC32 checksum for fast dedup / partition routing
SELECT CRYPTO_CRC32(id || created_at) AS checksum FROM events;

-- Random UUID generation
SELECT CRYPTO_RANDOM_UUID() AS event_id, payload FROM events;
```

## Notes

**AES encryption:** Uses AES-256-CBC. The key string is hashed to 32 bytes via SHA-256 internally, so any-length key is accepted. A random 16-byte IV is generated per call and prepended to the ciphertext — output is `base64(iv + ciphertext)`. This means encrypting the same value twice produces different ciphertext each time. `CRYPTO_AES_DECRYPT` expects the exact output format of `CRYPTO_AES_ENCRYPT`.

**MD5 / SHA-1:** Included for compatibility with legacy systems (e.g. matching existing MD5 hashes in a database). For new security-sensitive use cases, prefer SHA-256 or SHA-512.

**Timing attacks:** Use `CRYPTO_CONSTANT_TIME_EQUALS` when comparing hash values or tokens. Regular string equality (`=`) leaks timing information that can be exploited to guess valid tokens character by character.

## Tests

- **49/49 unit tests** passing (`mvn test`)
- **15/15 live tests** verified against Dremio 26.0.5
