package com.dremio.community.udf.finance;

import org.junit.Test;
import static org.junit.Assert.*;

public class FinanceMathTest {

    private static final double EPS = 1e-6;

    // ── TVM ───────────────────────────────────────────────────────────────────

    @Test public void testPv() {
        // PV of $100/yr for 5 yrs at 5% ≈ -432.95 (negative = cost to produce that cash flow)
        assertEquals(-432.9476, FinanceMath.pv(0.05, 5, 100, 0), 1e-3);
    }

    @Test public void testPvZeroRate() {
        assertEquals(-500.0, FinanceMath.pv(0, 5, 100, 0), EPS);
    }

    @Test public void testFv() {
        // FV of paying $100/yr for 5 yrs at 5% ≈ -552.56 (negative = accumulated value)
        assertEquals(-552.5631, FinanceMath.fv(0.05, 5, 100, 0), 1e-3);
    }

    @Test public void testPmt() {
        // Monthly payment on $200,000 loan, 0.5%/mo, 360 payments
        assertEquals(-1199.1010, FinanceMath.pmt(0.005, 360, 200000), 1e-3);
    }

    @Test public void testNper() {
        // How many periods to pay off $1000 at 1%/period with $100/period payments → ≈ 10.59
        double n = FinanceMath.nper(0.01, -100, 1000);
        assertEquals(10.59, n, 0.01);
    }

    @Test public void testRate() {
        // Rate for 360 payments of -1199.10 on $200,000 ≈ 0.5%
        assertEquals(0.005, FinanceMath.rate(360, -1199.10, 200000), 1e-4);
    }

    // ── NPV / IRR ─────────────────────────────────────────────────────────────

    @Test public void testNpv() {
        // NPV(0.10, 300, 400, 500) = 300/1.1 + 400/1.21 + 500/1.331 ≈ 978.97
        assertEquals(978.97, FinanceMath.npv(0.10, "300,400,500"), 0.01);
    }

    @Test public void testIrr() {
        // -1000 + 300/(1+r) + 400/(1+r)^2 + 500/(1+r)^3 = 0 → r ≈ 8.90%
        double irr = FinanceMath.irr("-1000,300,400,500");
        assertEquals(0.0890, irr, 1e-3);
    }

    // ── Interest ──────────────────────────────────────────────────────────────

    @Test public void testCompoundInterest() {
        // $1000 at 5% compounded monthly for 3 years → 1161.47
        assertEquals(1161.47, FinanceMath.compoundInterest(1000, 0.05, 12, 3), 0.01);
    }

    @Test public void testSimpleInterest() {
        assertEquals(150.0, FinanceMath.simpleInterest(1000, 0.05, 3), EPS);
    }

    @Test public void testCagr() {
        // 1000 → 1500 over 5 years: CAGR = (1.5)^(1/5) - 1 ≈ 8.45%
        assertEquals(0.0845, FinanceMath.cagr(1000, 1500, 5), 1e-3);
    }

    // ── Amortization ──────────────────────────────────────────────────────────

    @Test public void testAmortPayment() {
        double pmt = FinanceMath.amortPayment(200000, 0.005, 360);
        assertEquals(-1199.10, pmt, 0.01);
    }

    @Test public void testAmortInterestFirstPeriod() {
        // First period interest = 200000 * 0.005 = 1000
        assertEquals(1000.0, FinanceMath.amortInterest(200000, 0.005, 360, 1), 0.01);
    }

    @Test public void testAmortBalance() {
        // After full term, balance should be 0
        assertEquals(0.0, FinanceMath.amortBalance(200000, 0.005, 360, 360), 1.0);
    }

    @Test public void testAmortPrincipalPlusInterestEqualsPayment() {
        double payment = Math.abs(FinanceMath.amortPayment(200000, 0.005, 360));
        double interest = FinanceMath.amortInterest(200000, 0.005, 360, 10);
        double principal = FinanceMath.amortPrincipal(200000, 0.005, 360, 10);
        assertEquals(payment, interest + principal, 0.01);
    }

    // ── Black-Scholes ─────────────────────────────────────────────────────────

    @Test public void testBsCall() {
        // S=100, K=100, T=1, r=0.05, sigma=0.2 → call ≈ 10.45
        assertEquals(10.45, FinanceMath.bsCall(100, 100, 1, 0.05, 0.2), 0.05);
    }

    @Test public void testBsPut() {
        // Put-call parity: C - P = S - K*e^(-rT)
        double call = FinanceMath.bsCall(100, 100, 1, 0.05, 0.2);
        double put  = FinanceMath.bsPut(100, 100, 1, 0.05, 0.2);
        double parity = 100 - 100 * Math.exp(-0.05);
        assertEquals(parity, call - put, 0.01);
    }

    @Test public void testBsCallExpired() {
        assertEquals(5.0, FinanceMath.bsCall(105, 100, 0, 0.05, 0.2), EPS);
    }

    // ── Bond Pricing ──────────────────────────────────────────────────────────

    @Test public void testBondPriceAtPar() {
        // coupon rate == YTM → price == face value
        assertEquals(1000.0, FinanceMath.bondPrice(1000, 0.05, 0.05, 10), 1e-6);
    }

    @Test public void testBondPricePremium() {
        // coupon > YTM → price > face
        assertTrue(FinanceMath.bondPrice(1000, 0.06, 0.05, 10) > 1000);
    }

    @Test public void testBondDuration() {
        double d = FinanceMath.bondDuration(1000, 0.05, 0.05, 10);
        assertTrue(d > 7 && d < 9);
    }

    @Test public void testBondYtm() {
        // Round-trip: compute price then recover YTM
        double price = FinanceMath.bondPrice(1000, 0.05, 0.06, 10);
        assertEquals(0.06, FinanceMath.bondYtm(price, 1000, 0.05, 10), 1e-5);
    }

    // ── Depreciation ─────────────────────────────────────────────────────────

    @Test public void testDepreciationSl() {
        assertEquals(900.0, FinanceMath.depreciationSL(10000, 1000, 10), EPS);
    }

    @Test public void testDepreciationDb() {
        double d1 = FinanceMath.depreciationDB(10000, 1000, 10, 1);
        double d2 = FinanceMath.depreciationDB(10000, 1000, 10, 2);
        assertTrue(d1 > d2); // declining
    }

    @Test public void testDepreciationSyd() {
        double d1 = FinanceMath.depreciationSYD(10000, 1000, 10, 1);
        double d10 = FinanceMath.depreciationSYD(10000, 1000, 10, 10);
        assertTrue(d1 > d10);
    }

    // ── Ratios ────────────────────────────────────────────────────────────────

    @Test public void testRoi() {
        assertEquals(0.50, FinanceMath.roi(1500, 1000), EPS);
    }

    @Test public void testWacc() {
        // 60% equity @ 10%, 40% debt @ 5% after 30% tax
        double w = FinanceMath.wacc(600, 400, 0.10, 0.05, 0.30);
        assertEquals(0.074, w, 1e-4);
    }

    @Test public void testGrossMargin() {
        assertEquals(0.40, FinanceMath.grossMargin(1000, 600), EPS);
    }

    @Test public void testNetMargin() {
        assertEquals(0.10, FinanceMath.netMargin(100, 1000), EPS);
    }

    @Test public void testOperatingMargin() {
        assertEquals(0.15, FinanceMath.operatingMargin(150, 1000), EPS);
    }

    @Test public void testEps() {
        assertEquals(2.0, FinanceMath.eps(2000000, 1000000), EPS);
    }

    @Test public void testPeRatio() {
        assertEquals(25.0, FinanceMath.peRatio(50, 2), EPS);
    }

    @Test public void testEv() {
        assertEquals(1200.0, FinanceMath.ev(1000, 500, 300), EPS);
    }

    @Test public void testDebtToEquity() {
        assertEquals(0.5, FinanceMath.debtToEquity(500, 1000), EPS);
    }

    @Test public void testCurrentRatio() {
        assertEquals(2.0, FinanceMath.currentRatio(200, 100), EPS);
    }

    @Test public void testQuickRatio() {
        assertEquals(1.5, FinanceMath.quickRatio(100, 50, 100), EPS);
    }
}
