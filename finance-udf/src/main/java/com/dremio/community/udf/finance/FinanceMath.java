/*
 * Dremio Finance UDF — Pure-Java math helpers (no external deps).
 * All methods are package-accessible so UDF inner classes can call them.
 */
package com.dremio.community.udf.finance;

public final class FinanceMath {

    private FinanceMath() {}

    // ── Time Value of Money ────────────────────────────────────────────────────

    /** Present value: PV = PMT * (1 - (1+r)^-n) / r  [+ FV/(1+r)^n] */
    public static double pv(double rate, double nper, double pmt, double fv) {
        if (rate == 0) return -pmt * nper - fv;
        double factor = Math.pow(1 + rate, nper);
        return -(pmt * (factor - 1) / (rate * factor) + fv / factor);
    }

    /** Future value: FV = -PV*(1+r)^n - PMT*(((1+r)^n - 1)/r) */
    public static double fv(double rate, double nper, double pmt, double pv) {
        if (rate == 0) return -pv - pmt * nper;
        double factor = Math.pow(1 + rate, nper);
        return -pv * factor - pmt * (factor - 1) / rate;
    }

    /** Payment for a loan: PMT = r*PV / (1 - (1+r)^-n) */
    public static double pmt(double rate, double nper, double pv) {
        if (rate == 0) return -pv / nper;
        double factor = Math.pow(1 + rate, nper);
        return -pv * rate * factor / (factor - 1);
    }

    /** Number of periods: from PV*(1+r)^n + PMT*((1+r)^n-1)/r = 0 → n = ln(PMT/(PMT+r*PV)) / ln(1+r) */
    public static double nper(double rate, double pmt, double pv) {
        if (rate == 0) return -pv / pmt;
        return Math.log(pmt / (pmt + rate * pv)) / Math.log(1 + rate);
    }

    /** Rate via Newton-Raphson (30 iterations). */
    public static double rate(double nper, double pmt, double pv) {
        double r = 0.1;
        for (int i = 0; i < 100; i++) {
            double factor = Math.pow(1 + r, nper);
            double f  = pv * factor + pmt * (factor - 1) / r;
            double df = pv * nper * Math.pow(1 + r, nper - 1)
                       + pmt * (nper * r * Math.pow(1 + r, nper - 1) - (factor - 1)) / (r * r);
            double rNew = r - f / df;
            if (Math.abs(rNew - r) < 1e-10) return rNew;
            r = rNew;
        }
        return r;
    }

    // ── NPV / IRR ──────────────────────────────────────────────────────────────

    /** NPV of comma-separated cash flows (first flow is period 1). */
    public static double npv(double rate, String cashflowsCsv) {
        String[] parts = cashflowsCsv.split(",");
        double result = 0;
        for (int i = 0; i < parts.length; i++) {
            double cf = Double.parseDouble(parts[i].trim());
            result += cf / Math.pow(1 + rate, i + 1);
        }
        return result;
    }

    /** IRR via Newton-Raphson. cashflowsCsv[0] is the initial outflow (negative). */
    public static double irr(String cashflowsCsv) {
        String[] parts = cashflowsCsv.split(",");
        double[] cf = new double[parts.length];
        for (int i = 0; i < parts.length; i++) cf[i] = Double.parseDouble(parts[i].trim());
        double r = 0.1;
        for (int iter = 0; iter < 200; iter++) {
            double f = 0, df = 0;
            for (int i = 0; i < cf.length; i++) {
                double discount = Math.pow(1 + r, i);
                f  += cf[i] / discount;
                df -= i * cf[i] / (discount * (1 + r));
            }
            if (Math.abs(df) < 1e-14) break;
            double rNew = r - f / df;
            if (Math.abs(rNew - r) < 1e-10) return rNew;
            r = rNew;
        }
        return r;
    }

    // ── Interest ───────────────────────────────────────────────────────────────

    /** A = P*(1 + r/n)^(n*t) */
    public static double compoundInterest(double principal, double annualRate,
                                          double periodsPerYear, double years) {
        return principal * Math.pow(1 + annualRate / periodsPerYear, periodsPerYear * years);
    }

    /** I = P*r*t */
    public static double simpleInterest(double principal, double rate, double time) {
        return principal * rate * time;
    }

    /** CAGR = (end/start)^(1/years) - 1 */
    public static double cagr(double startValue, double endValue, double years) {
        return Math.pow(endValue / startValue, 1.0 / years) - 1;
    }

    // ── Amortization ──────────────────────────────────────────────────────────

    public static double amortPayment(double principal, double monthlyRate, int nper) {
        return pmt(monthlyRate, nper, principal);
    }

    /** Interest component of payment in given period (1-based). */
    public static double amortInterest(double principal, double monthlyRate, int nper, int period) {
        double payment = Math.abs(pmt(monthlyRate, nper, principal));
        double balance = principal;
        for (int i = 1; i < period; i++) {
            double interest  = balance * monthlyRate;
            balance -= (payment - interest);
        }
        return balance * monthlyRate;
    }

    /** Principal component of payment in given period (1-based). */
    public static double amortPrincipal(double principal, double monthlyRate, int nper, int period) {
        double payment  = Math.abs(pmt(monthlyRate, nper, principal));
        double interest = amortInterest(principal, monthlyRate, nper, period);
        return payment - interest;
    }

    /** Remaining balance after given period (1-based). */
    public static double amortBalance(double principal, double monthlyRate, int nper, int period) {
        double payment = Math.abs(pmt(monthlyRate, nper, principal));
        double balance = principal;
        for (int i = 0; i < period; i++) {
            double interest = balance * monthlyRate;
            balance -= (payment - interest);
        }
        return Math.max(0, balance);
    }

    // ── Black-Scholes ──────────────────────────────────────────────────────────

    private static double normCdf(double x) {
        // Abramowitz & Stegun approximation
        double t = 1.0 / (1 + 0.2316419 * Math.abs(x));
        double poly = t * (0.319381530 + t * (-0.356563782 + t * (1.781477937
                      + t * (-1.821255978 + t * 1.330274429))));
        double cdf = 1 - (1.0 / Math.sqrt(2 * Math.PI)) * Math.exp(-0.5 * x * x) * poly;
        return x >= 0 ? cdf : 1 - cdf;
    }

    public static double bsCall(double S, double K, double T, double r, double sigma) {
        if (T <= 0) return Math.max(S - K, 0);
        double d1 = (Math.log(S / K) + (r + 0.5 * sigma * sigma) * T) / (sigma * Math.sqrt(T));
        double d2 = d1 - sigma * Math.sqrt(T);
        return S * normCdf(d1) - K * Math.exp(-r * T) * normCdf(d2);
    }

    public static double bsPut(double S, double K, double T, double r, double sigma) {
        if (T <= 0) return Math.max(K - S, 0);
        double d1 = (Math.log(S / K) + (r + 0.5 * sigma * sigma) * T) / (sigma * Math.sqrt(T));
        double d2 = d1 - sigma * Math.sqrt(T);
        return K * Math.exp(-r * T) * normCdf(-d2) - S * normCdf(-d1);
    }

    // ── Bond Pricing ──────────────────────────────────────────────────────────

    /** Flat (clean) bond price: PV of coupons + PV of face. */
    public static double bondPrice(double faceValue, double couponRate, double ytm, int periods) {
        double coupon = faceValue * couponRate;
        double price = 0;
        for (int i = 1; i <= periods; i++) price += coupon / Math.pow(1 + ytm, i);
        price += faceValue / Math.pow(1 + ytm, periods);
        return price;
    }

    /** Macaulay duration (in periods). */
    public static double bondDuration(double faceValue, double couponRate, double ytm, int periods) {
        double coupon = faceValue * couponRate;
        double price = bondPrice(faceValue, couponRate, ytm, periods);
        double duration = 0;
        for (int i = 1; i <= periods; i++) duration += i * coupon / Math.pow(1 + ytm, i);
        duration += periods * faceValue / Math.pow(1 + ytm, periods);
        return duration / price;
    }

    /** YTM via Newton-Raphson. */
    public static double bondYtm(double price, double faceValue, double couponRate, int periods) {
        double coupon = faceValue * couponRate;
        double ytm = couponRate;
        for (int iter = 0; iter < 200; iter++) {
            double f = -price, df = 0;
            for (int i = 1; i <= periods; i++) {
                double d = Math.pow(1 + ytm, i);
                f  += coupon / d;
                df -= i * coupon / (d * (1 + ytm));
            }
            double d = Math.pow(1 + ytm, periods);
            f  += faceValue / d;
            df -= periods * faceValue / (d * (1 + ytm));
            if (Math.abs(df) < 1e-14) break;
            double ytmNew = ytm - f / df;
            if (Math.abs(ytmNew - ytm) < 1e-10) return ytmNew;
            ytm = ytmNew;
        }
        return ytm;
    }

    // ── Depreciation ──────────────────────────────────────────────────────────

    /** Straight-line depreciation per period. */
    public static double depreciationSL(double cost, double salvage, double life) {
        return (cost - salvage) / life;
    }

    /** Declining balance depreciation for a given period (1-based). */
    public static double depreciationDB(double cost, double salvage, double life, int period) {
        double rate = 1 - Math.pow(salvage / cost, 1.0 / life);
        double bookValue = cost * Math.pow(1 - rate, period - 1);
        return bookValue * rate;
    }

    /** Sum-of-years digits depreciation for a given period (1-based). */
    public static double depreciationSYD(double cost, double salvage, double life, int period) {
        double syd = life * (life + 1) / 2.0;
        return (cost - salvage) * (life - period + 1) / syd;
    }

    // ── Ratios / Returns ──────────────────────────────────────────────────────

    public static double roi(double gain, double cost) {
        return (gain - cost) / cost;
    }

    public static double wacc(double equityValue, double debtValue,
                              double costOfEquity, double costOfDebt, double taxRate) {
        double total = equityValue + debtValue;
        return (equityValue / total) * costOfEquity
             + (debtValue  / total) * costOfDebt * (1 - taxRate);
    }

    public static double grossMargin(double revenue, double cogs) {
        return (revenue - cogs) / revenue;
    }

    public static double netMargin(double netIncome, double revenue) {
        return netIncome / revenue;
    }

    public static double operatingMargin(double operatingIncome, double revenue) {
        return operatingIncome / revenue;
    }

    public static double eps(double netIncome, double sharesOutstanding) {
        return netIncome / sharesOutstanding;
    }

    public static double peRatio(double pricePerShare, double epsValue) {
        return pricePerShare / epsValue;
    }

    public static double ev(double marketCap, double totalDebt, double cashEquivalents) {
        return marketCap + totalDebt - cashEquivalents;
    }

    public static double debtToEquity(double totalDebt, double totalEquity) {
        return totalDebt / totalEquity;
    }

    public static double currentRatio(double currentAssets, double currentLiabilities) {
        return currentAssets / currentLiabilities;
    }

    public static double quickRatio(double cash, double receivables, double currentLiabilities) {
        return (cash + receivables) / currentLiabilities;
    }
}
