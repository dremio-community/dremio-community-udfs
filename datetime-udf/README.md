# Dremio Date/Time UDF Library

*Built by Mark Shainman*

24 scalar UDFs for fiscal calendars, business day arithmetic, period boundaries, date diff, epoch milliseconds, and date formatting in Dremio SQL. No external runtime dependencies — pure Java 11.

Dremio has `DATE_TRUNC`, `LAST_DAY`, `DATEDIFF` (days only), and `TO_UNIX_TIMESTAMP` (seconds only). This library fills the gaps: fiscal year/quarter/month math, business day counting and navigation, period end boundaries, diff in months/years, millisecond epoch conversions, and `DATE_FORMAT`.

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

After restart, all `DT_*` functions are available in SQL immediately.

---

## Functions

### Fiscal Calendar

All fiscal functions take `(DATE, INT fiscal_start_month)`.  
`fiscal_start_month`: `1` = January (calendar year), `4` = April (UK), `7` = July (AU/US federal), `10` = October.

Convention: the fiscal year that **begins** in `fiscal_start_month` of calendar year Y is named **FY(Y+1)**.  
Example with `fiscal_start_month=7`: `2023-07-01` → FY2024, `2024-01-15` → FY2024, `2024-07-01` → FY2025.

| Function | Returns | Description |
|---|---|---|
| `DT_FISCAL_YEAR(date, start_month)` | INT | Fiscal year number |
| `DT_FISCAL_QUARTER(date, start_month)` | INT | Fiscal quarter (1–4) |
| `DT_FISCAL_MONTH(date, start_month)` | INT | Fiscal month (1–12) |
| `DT_FISCAL_WEEK(date, start_month)` | INT | Week number within the fiscal year |
| `DT_FISCAL_YEAR_START(date, start_month)` | DATE | First day of the fiscal year |
| `DT_FISCAL_YEAR_END(date, start_month)` | DATE | Last day of the fiscal year |
| `DT_FISCAL_QUARTER_START(date, start_month)` | DATE | First day of the fiscal quarter |
| `DT_FISCAL_QUARTER_END(date, start_month)` | DATE | Last day of the fiscal quarter |

### Business Days

Weekday math only — no holiday calendar. Saturdays and Sundays are non-business days.

| Function | Returns | Description |
|---|---|---|
| `DT_IS_WEEKDAY(date)` | BIT | 1 if Mon–Fri, 0 if Sat–Sun |
| `DT_BIZDAYS_BETWEEN(start, end)` | INT | Weekdays in `[start, end)` — negative if start > end |
| `DT_ADD_BIZDAYS(date, n)` | DATE | Date + n business days (negative n goes backwards) |
| `DT_NEXT_WEEKDAY(date)` | DATE | Next Mon–Fri after date |
| `DT_PREV_WEEKDAY(date)` | DATE | Previous Mon–Fri before date |

### Period End Boundaries

Dremio's `DATE_TRUNC` gives period starts; `LAST_DAY` gives month end. These add the missing end boundaries.

| Function | Returns | Description |
|---|---|---|
| `DT_WEEK_END(date)` | DATE | Sunday of the ISO week containing date |
| `DT_QUARTER_END(date)` | DATE | Last day of the calendar quarter |
| `DT_YEAR_END(date)` | DATE | December 31 of the year |
| `DT_DAYS_IN_MONTH(date)` | INT | Number of days in the month (28–31) |

### Date Arithmetic

`DATEDIFF` only returns days; these add months, years, and proper age calculation.

| Function | Returns | Description |
|---|---|---|
| `DT_DIFF_MONTHS(start, end)` | INT | Complete months between dates (can be negative) |
| `DT_DIFF_YEARS(start, end)` | INT | Complete years between dates (can be negative) |
| `DT_AGE_YEARS(birth_date, as_of)` | INT | Age in completed years as of a reference date |
| `DT_IS_LEAP_YEAR(date)` | BIT | 1 if the year is a leap year |

### Unix Epoch (Milliseconds)

Dremio's `TO_UNIX_TIMESTAMP` / `FROM_UNIXTIME` only handle seconds. These add millisecond precision, required when working with Kafka, JavaScript, or any system that uses epoch milliseconds.

| Function | Returns | Description |
|---|---|---|
| `DT_TO_UNIX_MILLIS(timestamp)` | BIGINT | Timestamp → epoch milliseconds |
| `DT_FROM_UNIX_MILLIS(millis)` | TIMESTAMP | Epoch milliseconds → timestamp |
| `DT_TO_UNIX_SECONDS(timestamp)` | BIGINT | Timestamp → epoch seconds (integer, no fractional) |
| `DT_FROM_UNIX_SECONDS(seconds)` | TIMESTAMP | Epoch seconds → timestamp |

### Date Formatting

`DATE_FORMAT` is missing/broken in Dremio. These fill that gap using strftime-style tokens.

| Function | Returns | Description |
|---|---|---|
| `DT_FORMAT(date, pattern)` | VARCHAR | Format a DATE value |
| `DT_FORMAT_TS(timestamp, pattern)` | VARCHAR | Format a TIMESTAMP value (date part only) |

**Supported tokens:** `%Y` (4-digit year), `%y` (2-digit year), `%m` (month 01–12), `%d` (day 01–31), `%e` (day 1–31, no padding), `%A` (full weekday), `%a` (short weekday), `%B` (full month name), `%b` (short month name), `%j` (day of year), `%W` (ISO week number).

---

## SQL Examples

```sql
-- Fiscal calendar (start_month=7 = Australian/US federal fiscal year)
SELECT
  DT_FISCAL_YEAR(sale_date, 7)            AS fiscal_year,
  DT_FISCAL_QUARTER(sale_date, 7)         AS fiscal_quarter,
  DT_FISCAL_YEAR_START(sale_date, 7)      AS fy_start,
  DT_FISCAL_YEAR_END(sale_date, 7)        AS fy_end
FROM sales;

-- Group by fiscal quarter
SELECT
  DT_FISCAL_YEAR(order_date, 4)           AS fy,      -- UK fiscal (Apr start)
  DT_FISCAL_QUARTER(order_date, 4)        AS fq,
  SUM(amount)                             AS revenue
FROM orders
GROUP BY 1, 2
ORDER BY 1, 2;

-- Business day calculations
SELECT
  DT_BIZDAYS_BETWEEN(start_date, due_date)  AS biz_days_remaining,
  DT_ADD_BIZDAYS(CURRENT_DATE, 5)           AS five_biz_days_from_now,
  DT_NEXT_WEEKDAY(CURRENT_DATE)             AS next_business_day
FROM contracts;

-- Age calculation for customer segmentation
SELECT
  customer_id,
  DT_AGE_YEARS(date_of_birth, CURRENT_DATE) AS age,
  CASE
    WHEN DT_AGE_YEARS(date_of_birth, CURRENT_DATE) < 25 THEN 'Gen Z'
    WHEN DT_AGE_YEARS(date_of_birth, CURRENT_DATE) < 40 THEN 'Millennial'
    ELSE 'Gen X+'
  END AS segment
FROM customers;

-- Date diff in months for cohort analysis
SELECT
  customer_id,
  DT_DIFF_MONTHS(first_purchase_date, CURRENT_DATE) AS months_since_first_purchase
FROM customers;

-- Period end boundaries
SELECT
  DT_QUARTER_END(CURRENT_DATE)              AS quarter_close,
  DT_YEAR_END(CURRENT_DATE)                 AS year_close,
  DT_WEEK_END(CURRENT_DATE)                 AS week_close,
  DT_DAYS_IN_MONTH(CURRENT_DATE)            AS days_in_current_month
FROM (VALUES(1)) t(x);

-- Unix millisecond epoch (Kafka/event streams)
SELECT
  DT_TO_UNIX_MILLIS(event_timestamp)        AS epoch_ms,
  DT_FROM_UNIX_MILLIS(kafka_offset_ts)      AS event_time
FROM event_log;

-- Date formatting
SELECT
  DT_FORMAT(order_date, '%B %d, %Y')        AS formatted_date,
  DT_FORMAT(order_date, '%Y-%m')            AS year_month,
  DT_FORMAT(order_date, '%A')               AS day_of_week
FROM orders;
```

---

## Choosing the Right Function

| Use case | Recommended function |
|---|---|
| Group revenue by fiscal quarter | `DT_FISCAL_YEAR` + `DT_FISCAL_QUARTER` |
| SLA compliance (business days) | `DT_BIZDAYS_BETWEEN` |
| Settlement date calculation | `DT_ADD_BIZDAYS` |
| Age-based segmentation | `DT_AGE_YEARS` |
| Cohort analysis by months | `DT_DIFF_MONTHS` |
| Event stream timestamp (ms epoch) | `DT_TO_UNIX_MILLIS` / `DT_FROM_UNIX_MILLIS` |
| Report date labels | `DT_FORMAT` |
| Quarter-end close date | `DT_QUARTER_END` |
| Is today a business day? | `DT_IS_WEEKDAY` |
| Leap year check | `DT_IS_LEAP_YEAR` |

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

67 unit tests covering all utility methods. Run without a Dremio instance:

```bash
# Inside the Dremio container (has Java + Maven)
docker exec try-dremio bash -c "cd /tmp/datetime-build && mvn test"
```

**Result:** 67/67 tests passing.
