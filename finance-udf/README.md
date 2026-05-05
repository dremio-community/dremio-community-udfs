# Dremio Finance UDF

A library of **30 scalar UDFs** for financial mathematics in Dremio SQL.  
Pure Java — no external dependencies beyond the Dremio/Arrow runtime.

## Functions

### Time Value of Money

| Function | Arguments | Returns | Description |
|---|---|---|---|
| `FIN_PV(rate, nper, pmt)` | rate FLOAT, nper FLOAT, pmt FLOAT | FLOAT | Present value of annuity |
| `FIN_FV(rate, nper, pmt, pv)` | rate FLOAT, nper FLOAT, pmt FLOAT, pv FLOAT | FLOAT | Future value |
| `FIN_PMT(rate, nper, pv)` | rate FLOAT, nper FLOAT, pv FLOAT | FLOAT | Periodic payment for a loan |
| `FIN_NPER(rate, pmt, pv)` | rate FLOAT, pmt FLOAT, pv FLOAT | FLOAT | Number of periods |
| `FIN_RATE(nper, pmt, pv)` | nper FLOAT, pmt FLOAT, pv FLOAT | FLOAT | Periodic interest rate (Newton-Raphson) |

### NPV & IRR

| Function | Arguments | Returns | Description |
|---|---|---|---|
| `FIN_NPV(rate, cashflows_csv)` | rate FLOAT, cashflows VARCHAR | FLOAT | Net present value from comma-separated cash flows (t=1 onwards) |
| `FIN_IRR(cashflows_csv)` | cashflows VARCHAR | FLOAT | Internal rate of return (Newton-Raphson); first value is t=0 |

### Interest & Growth

| Function | Arguments | Returns | Description |
|---|---|---|---|
| `FIN_COMPOUND_INTEREST(principal, annual_rate, periods_per_year, years)` | all FLOAT | FLOAT | Compound interest: P×(1 + r/n)^(n×t) |
| `FIN_SIMPLE_INTEREST(principal, rate, time)` | all FLOAT | FLOAT | Simple interest: P×r×t |
| `FIN_CAGR(start_value, end_value, years)` | all FLOAT | FLOAT | Compound annual growth rate |

### Loan Amortization

All functions are 1-based (period 1 = first payment). `rate` is the per-period rate (e.g. `annual_rate / 12`).

| Function | Arguments | Returns | Description |
|---|---|---|---|
| `FIN_AMORT_PAYMENT(principal, rate, nper)` | all FLOAT | FLOAT | Periodic payment amount |
| `FIN_AMORT_INTEREST(principal, rate, nper, period)` | all FLOAT | FLOAT | Interest component in a given period |
| `FIN_AMORT_PRINCIPAL(principal, rate, nper, period)` | all FLOAT | FLOAT | Principal component in a given period |
| `FIN_AMORT_BALANCE(principal, rate, nper, period)` | all FLOAT | FLOAT | Remaining balance after a given period |

### Black-Scholes Options Pricing

| Function | Arguments | Returns | Description |
|---|---|---|---|
| `FIN_BS_CALL(S, K, T, r, sigma)` | all FLOAT | FLOAT | European call option price |
| `FIN_BS_PUT(S, K, T, r, sigma)` | all FLOAT | FLOAT | European put option price |

`S` = current stock price, `K` = strike price, `T` = time to expiry (years), `r` = risk-free rate (annual), `sigma` = volatility (annual).

### Bond Pricing

| Function | Arguments | Returns | Description |
|---|---|---|---|
| `FIN_BOND_PRICE(face_value, coupon_rate, ytm, periods)` | face/coupon/ytm FLOAT, periods FLOAT | FLOAT | Clean bond price (PV of coupons + face) |
| `FIN_BOND_DURATION(face_value, coupon_rate, ytm, periods)` | face/coupon/ytm FLOAT, periods FLOAT | FLOAT | Macaulay duration in periods |
| `FIN_BOND_YTM(price, face_value, coupon_rate, periods)` | price/face/coupon FLOAT, periods FLOAT | FLOAT | Yield to maturity (Newton-Raphson) |

### Depreciation

| Function | Arguments | Returns | Description |
|---|---|---|---|
| `FIN_DEPRECIATION_SL(cost, salvage, life)` | all FLOAT | FLOAT | Straight-line depreciation per period |
| `FIN_DEPRECIATION_DB(cost, salvage, life, period)` | all FLOAT | FLOAT | Declining balance (1-based period) |
| `FIN_DEPRECIATION_SYD(cost, salvage, life, period)` | all FLOAT | FLOAT | Sum-of-years-digits (1-based period) |

### Financial Ratios

| Function | Arguments | Returns | Description |
|---|---|---|---|
| `FIN_ROI(gain, cost)` | FLOAT, FLOAT | FLOAT | Return on investment: (gain − cost) / cost |
| `FIN_WACC(equity, debt, cost_equity, cost_debt, tax_rate)` | all FLOAT | FLOAT | Weighted average cost of capital |
| `FIN_GROSS_MARGIN(revenue, cogs)` | FLOAT, FLOAT | FLOAT | Gross margin percentage |
| `FIN_NET_MARGIN(net_income, revenue)` | FLOAT, FLOAT | FLOAT | Net profit margin |
| `FIN_OPERATING_MARGIN(op_income, revenue)` | FLOAT, FLOAT | FLOAT | Operating margin |
| `FIN_EPS(net_income, shares_outstanding)` | FLOAT, FLOAT | FLOAT | Earnings per share |
| `FIN_PE_RATIO(price_per_share, eps)` | FLOAT, FLOAT | FLOAT | Price-to-earnings ratio |
| `FIN_EV(market_cap, total_debt, cash)` | all FLOAT | FLOAT | Enterprise value |
| `FIN_DEBT_TO_EQUITY(total_debt, total_equity)` | FLOAT, FLOAT | FLOAT | Debt-to-equity ratio |
| `FIN_CURRENT_RATIO(current_assets, current_liabilities)` | FLOAT, FLOAT | FLOAT | Current ratio |
| `FIN_QUICK_RATIO(cash, receivables, current_liabilities)` | all FLOAT | FLOAT | Quick (acid-test) ratio |

## Usage Examples

```sql
-- Monthly loan payment on a $300,000 mortgage at 6% APR, 30 years
SELECT fin_pmt(0.06/12, 360, 300000) AS monthly_payment;
-- → -1798.65

-- Present value of $500/month for 10 years at 5% APR
SELECT fin_pv(0.05/12, 120, 500) AS pv;

-- NPV of a project: invest $5,000 now, receive $2,000/yr for 3 years at 8%
SELECT -5000 + fin_npv(0.08, '2000,2000,2000') AS npv;

-- IRR of: invest -10000, receive 3000, 4000, 5000
SELECT fin_irr('-10000,3000,4000,5000') AS irr;

-- Black-Scholes call: stock=$150, strike=$145, T=0.5yr, r=4%, vol=25%
SELECT fin_bs_call(150, 145, 0.5, 0.04, 0.25) AS call_price;

-- Bond YTM: 5% coupon bond, face=$1000, price=$950, 10 periods
SELECT fin_bond_ytm(950, 1000, 0.05, 10) AS ytm;

-- Amortization schedule for period 1 of a $200,000 loan at 6%/yr, 360 payments
SELECT
  fin_amort_payment(200000, 0.005, 360) AS payment,
  fin_amort_interest(200000, 0.005, 360, 1) AS interest,
  fin_amort_principal(200000, 0.005, 360, 1) AS principal,
  fin_amort_balance(200000, 0.005, 360, 1) AS balance;

-- Gross and net margin from an income table
SELECT
  revenue,
  fin_gross_margin(revenue, cogs) AS gross_margin,
  fin_net_margin(net_income, revenue) AS net_margin
FROM income_data;
```

## Sign Conventions

These functions follow the cash-flow sign convention used by Excel and most financial calculators:

- **Money you receive** → positive (e.g. loan principal as `pv`)
- **Money you pay out** → negative (e.g. `pmt` payments, `irr` initial investment)

So `fin_pmt(0.005, 360, 200000)` returns `-1199.10` (you pay $1,199/month).  
`fin_pv(0.05, 5, 100)` returns `-432.95` (you must invest $432.95 to produce those cash flows).

## Installation

### Prerequisites

- Running Dremio instance (Docker, bare-metal, or Kubernetes)
- Docker (for the default container-based install) or Maven (for bare-metal builds)

### Quick Install (prebuilt JAR)

```bash
git clone https://github.com/dremio-community/dremio-community-udfs.git
cd dremio-community-udfs/finance-udf
./install.sh --prebuilt
```

This copies `jars/dremio-finance-udf-1.0.0.jar` into the Dremio container and restarts it.

### Build from Source

```bash
./install.sh          # builds inside container, then installs
```

### Options

```
--docker  NAME    Container name (default: try-dremio)
--local   PATH    Bare-metal Dremio dir (e.g. /opt/dremio)
--k8s     POD     Kubernetes pod name
--prebuilt        Skip build, use jars/ directly
--no-restart      Don't restart Dremio after installing
```

### Upgrade (version drift)

```bash
./rebuild.sh          # detects running Dremio version, updates pom.xml, rebuilds, redeploys
```

## Building Manually

Inside the Dremio container (which has Maven pre-installed):

```bash
docker cp finance-udf/ try-dremio:/tmp/finance-udf-build
docker exec try-dremio bash -c "cd /tmp/finance-udf-build && mvn package -DskipTests"
docker cp try-dremio:/tmp/finance-udf-build/jars/dremio-finance-udf-1.0.0.jar \
    /opt/dremio/jars/3rdparty/
docker restart try-dremio
```
