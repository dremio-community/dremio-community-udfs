package com.dremio.community.udf.datetime;

import org.junit.Test;
import java.time.LocalDate;
import static org.junit.Assert.*;

public class DateTimeUtilsTest {

    // ── toLocalDate / toEpochMillis round-trip ───────────────────────────────

    @Test public void epochRoundTrip() {
        LocalDate d = LocalDate.of(2024, 3, 15);
        assertEquals(d, DateTimeUtils.toLocalDate(DateTimeUtils.toEpochMillis(d)));
    }
    @Test public void epochKnownDate() {
        LocalDate d = DateTimeUtils.toLocalDate(0L);
        assertEquals(LocalDate.of(1970, 1, 1), d);
    }

    // ── Fiscal Year (startMonth=7, AU/US federal style) ──────────────────────

    @Test public void fiscalYearJulBeforeStart() {
        // Jan 15 2024, FY starts July → FY2024
        assertEquals(2024, DateTimeUtils.fiscalYear(LocalDate.of(2024, 1, 15), 7));
    }
    @Test public void fiscalYearJulAfterStart() {
        // Aug 1 2023, FY starts July → FY2024
        assertEquals(2024, DateTimeUtils.fiscalYear(LocalDate.of(2023, 8, 1), 7));
    }
    @Test public void fiscalYearJulOnStart() {
        // Jul 1 2024, FY starts July → FY2025
        assertEquals(2025, DateTimeUtils.fiscalYear(LocalDate.of(2024, 7, 1), 7));
    }
    @Test public void fiscalYearCalendar() {
        // startMonth=1 → calendar year
        assertEquals(2024, DateTimeUtils.fiscalYear(LocalDate.of(2024, 6, 30), 1));
    }

    // ── Fiscal Quarter ───────────────────────────────────────────────────────

    @Test public void fiscalQuarterQ1Jul() {
        // Jul = FM1 → FQ1
        assertEquals(1, DateTimeUtils.fiscalQuarter(LocalDate.of(2023, 7, 15), 7));
    }
    @Test public void fiscalQuarterQ2Jul() {
        // Oct = FM4 → FQ2
        assertEquals(2, DateTimeUtils.fiscalQuarter(LocalDate.of(2023, 10, 1), 7));
    }
    @Test public void fiscalQuarterQ3Jul() {
        // Jan = FM7 → FQ3
        assertEquals(3, DateTimeUtils.fiscalQuarter(LocalDate.of(2024, 1, 1), 7));
    }
    @Test public void fiscalQuarterQ4Jul() {
        // Apr = FM10 → FQ4
        assertEquals(4, DateTimeUtils.fiscalQuarter(LocalDate.of(2024, 4, 1), 7));
    }

    // ── Fiscal Month ─────────────────────────────────────────────────────────

    @Test public void fiscalMonthStart() {
        // Jul = FM1 when startMonth=7
        assertEquals(1, DateTimeUtils.fiscalMonth(LocalDate.of(2023, 7, 1), 7));
    }
    @Test public void fiscalMonthWrap() {
        // Jun = FM12 when startMonth=7
        assertEquals(12, DateTimeUtils.fiscalMonth(LocalDate.of(2024, 6, 30), 7));
    }
    @Test public void fiscalMonthCalendar() {
        assertEquals(3, DateTimeUtils.fiscalMonth(LocalDate.of(2024, 3, 1), 1));
    }

    // ── Fiscal Year Start / End ───────────────────────────────────────────────

    @Test public void fiscalYearStartJul() {
        LocalDate d = LocalDate.of(2024, 1, 15); // FY2024, starts Jul 2023
        assertEquals(LocalDate.of(2023, 7, 1), DateTimeUtils.fiscalYearStart(d, 7));
    }
    @Test public void fiscalYearEndJul() {
        LocalDate d = LocalDate.of(2024, 1, 15); // FY2024, ends Jun 30 2024
        assertEquals(LocalDate.of(2024, 6, 30), DateTimeUtils.fiscalYearEnd(d, 7));
    }
    @Test public void fiscalYearStartCalendar() {
        assertEquals(LocalDate.of(2024, 1, 1), DateTimeUtils.fiscalYearStart(LocalDate.of(2024, 6, 15), 1));
    }

    // ── Fiscal Quarter Start / End ────────────────────────────────────────────

    @Test public void fiscalQtrStartQ1() {
        // Oct 15 2023, FY starts Jul → FQ2 starts Oct 1
        assertEquals(LocalDate.of(2023, 10, 1), DateTimeUtils.fiscalQuarterStart(LocalDate.of(2023, 10, 15), 7));
    }
    @Test public void fiscalQtrEndQ1() {
        // Jul 15 2023, FQ1 → ends Sep 30
        assertEquals(LocalDate.of(2023, 9, 30), DateTimeUtils.fiscalQuarterEnd(LocalDate.of(2023, 7, 15), 7));
    }

    // ── Fiscal Week ──────────────────────────────────────────────────────────

    @Test public void fiscalWeekFirst() {
        // Jul 1 = week 1 of FY (startMonth=7)
        assertEquals(1, DateTimeUtils.fiscalWeek(LocalDate.of(2023, 7, 1), 7));
    }
    @Test public void fiscalWeekSecond() {
        assertEquals(2, DateTimeUtils.fiscalWeek(LocalDate.of(2023, 7, 8), 7));
    }

    // ── isWeekday ────────────────────────────────────────────────────────────

    @Test public void isWeekdayMonday()   { assertTrue(DateTimeUtils.isWeekday(LocalDate.of(2024, 4, 22))); }
    @Test public void isWeekdayFriday()   { assertTrue(DateTimeUtils.isWeekday(LocalDate.of(2024, 4, 26))); }
    @Test public void isWeekdaySaturday() { assertFalse(DateTimeUtils.isWeekday(LocalDate.of(2024, 4, 27))); }
    @Test public void isWeekdaySunday()   { assertFalse(DateTimeUtils.isWeekday(LocalDate.of(2024, 4, 28))); }

    // ── bizdaysBetween ───────────────────────────────────────────────────────

    @Test public void bizdaysSameDay()      { assertEquals(0, DateTimeUtils.bizdaysBetween(LocalDate.of(2024,4,22), LocalDate.of(2024,4,22))); }
    @Test public void bizdaysOneWeek()      { assertEquals(5, DateTimeUtils.bizdaysBetween(LocalDate.of(2024,4,22), LocalDate.of(2024,4,29))); }
    @Test public void bizdaysMonToFri()     { assertEquals(4, DateTimeUtils.bizdaysBetween(LocalDate.of(2024,4,22), LocalDate.of(2024,4,26))); }
    @Test public void bizdaysAcrossWeekend(){ assertEquals(5, DateTimeUtils.bizdaysBetween(LocalDate.of(2024,4,26), LocalDate.of(2024,5,3))); }
    @Test public void bizdaysNegative()     { assertEquals(-4, DateTimeUtils.bizdaysBetween(LocalDate.of(2024,4,26), LocalDate.of(2024,4,22))); }

    // ── addBizdays ───────────────────────────────────────────────────────────

    @Test public void addBizdaysZero()     { assertEquals(LocalDate.of(2024,4,22), DateTimeUtils.addBizdays(LocalDate.of(2024,4,22), 0)); }
    @Test public void addBizdaysOne()      { assertEquals(LocalDate.of(2024,4,23), DateTimeUtils.addBizdays(LocalDate.of(2024,4,22), 1)); }
    @Test public void addBizdaysSkipWeekend() {
        // Friday + 1 biz = Monday
        assertEquals(LocalDate.of(2024,4,29), DateTimeUtils.addBizdays(LocalDate.of(2024,4,26), 1));
    }
    @Test public void addBizdaysFive()     { assertEquals(LocalDate.of(2024,4,29), DateTimeUtils.addBizdays(LocalDate.of(2024,4,22), 5)); }
    @Test public void addBizdaysNegative() {
        // Monday - 1 = Friday
        assertEquals(LocalDate.of(2024,4,26), DateTimeUtils.addBizdays(LocalDate.of(2024,4,29), -1));
    }

    // ── nextWeekday / prevWeekday ─────────────────────────────────────────────

    @Test public void nextWeekdayFromFriday()   { assertEquals(LocalDate.of(2024,4,29), DateTimeUtils.nextWeekday(LocalDate.of(2024,4,26))); }
    @Test public void nextWeekdayFromSaturday() { assertEquals(LocalDate.of(2024,4,29), DateTimeUtils.nextWeekday(LocalDate.of(2024,4,27))); }
    @Test public void nextWeekdayFromMonday()   { assertEquals(LocalDate.of(2024,4,23), DateTimeUtils.nextWeekday(LocalDate.of(2024,4,22))); }
    @Test public void prevWeekdayFromMonday()   { assertEquals(LocalDate.of(2024,4,26), DateTimeUtils.prevWeekday(LocalDate.of(2024,4,29))); }
    @Test public void prevWeekdayFromSunday()   { assertEquals(LocalDate.of(2024,4,26), DateTimeUtils.prevWeekday(LocalDate.of(2024,4,28))); }

    // ── weekEnd ──────────────────────────────────────────────────────────────

    @Test public void weekEndFromWednesday() { assertEquals(LocalDate.of(2024,4,28), DateTimeUtils.weekEnd(LocalDate.of(2024,4,24))); }
    @Test public void weekEndFromSunday()    { assertEquals(LocalDate.of(2024,4,28), DateTimeUtils.weekEnd(LocalDate.of(2024,4,28))); }

    // ── quarterEnd ───────────────────────────────────────────────────────────

    @Test public void quarterEndQ1() { assertEquals(LocalDate.of(2024,3,31), DateTimeUtils.quarterEnd(LocalDate.of(2024,1,15))); }
    @Test public void quarterEndQ2() { assertEquals(LocalDate.of(2024,6,30), DateTimeUtils.quarterEnd(LocalDate.of(2024,4,1))); }
    @Test public void quarterEndQ3() { assertEquals(LocalDate.of(2024,9,30), DateTimeUtils.quarterEnd(LocalDate.of(2024,7,31))); }
    @Test public void quarterEndQ4() { assertEquals(LocalDate.of(2024,12,31), DateTimeUtils.quarterEnd(LocalDate.of(2024,10,1))); }

    // ── yearEnd ──────────────────────────────────────────────────────────────

    @Test public void yearEnd() { assertEquals(LocalDate.of(2024,12,31), DateTimeUtils.yearEnd(LocalDate.of(2024,3,15))); }

    // ── daysInMonth ──────────────────────────────────────────────────────────

    @Test public void daysInMonthJan()     { assertEquals(31, DateTimeUtils.daysInMonth(LocalDate.of(2024,1,1))); }
    @Test public void daysInMonthFebLeap() { assertEquals(29, DateTimeUtils.daysInMonth(LocalDate.of(2024,2,1))); }
    @Test public void daysInMonthFebNonLeap() { assertEquals(28, DateTimeUtils.daysInMonth(LocalDate.of(2023,2,1))); }
    @Test public void daysInMonthApr()     { assertEquals(30, DateTimeUtils.daysInMonth(LocalDate.of(2024,4,1))); }

    // ── diffMonths / diffYears ───────────────────────────────────────────────

    @Test public void diffMonthsSameMonth()  { assertEquals(0, DateTimeUtils.diffMonths(LocalDate.of(2024,1,1), LocalDate.of(2024,1,31))); }
    @Test public void diffMonthsThree()      { assertEquals(3, DateTimeUtils.diffMonths(LocalDate.of(2024,1,1), LocalDate.of(2024,4,1))); }
    @Test public void diffMonthsNegative()   { assertEquals(-1, DateTimeUtils.diffMonths(LocalDate.of(2024,4,1), LocalDate.of(2024,3,1))); }
    @Test public void diffYearsTwo()         { assertEquals(2, DateTimeUtils.diffYears(LocalDate.of(2022,1,1), LocalDate.of(2024,1,1))); }
    @Test public void diffYearsPartialYear() { assertEquals(0, DateTimeUtils.diffYears(LocalDate.of(2024,1,1), LocalDate.of(2024,12,31))); }

    // ── ageYears ─────────────────────────────────────────────────────────────

    @Test public void ageYearsExact()      { assertEquals(30, DateTimeUtils.ageYears(LocalDate.of(1994,4,24), LocalDate.of(2024,4,24))); }
    @Test public void ageYearsBeforeBday() { assertEquals(29, DateTimeUtils.ageYears(LocalDate.of(1994,4,25), LocalDate.of(2024,4,24))); }
    @Test public void ageYearsAfterBday()  { assertEquals(30, DateTimeUtils.ageYears(LocalDate.of(1994,4,23), LocalDate.of(2024,4,24))); }

    // ── isLeapYear ───────────────────────────────────────────────────────────

    @Test public void leapYear2024()    { assertTrue(DateTimeUtils.isLeapYear(LocalDate.of(2024,1,1))); }
    @Test public void notLeapYear2023() { assertFalse(DateTimeUtils.isLeapYear(LocalDate.of(2023,1,1))); }
    @Test public void leapYear2000()    { assertTrue(DateTimeUtils.isLeapYear(LocalDate.of(2000,1,1))); }
    @Test public void notLeapYear1900() { assertFalse(DateTimeUtils.isLeapYear(LocalDate.of(1900,1,1))); }

    // ── formatDate ───────────────────────────────────────────────────────────

    @Test public void formatYearMonthDay() {
        assertEquals("2024-03-15", DateTimeUtils.formatDate(LocalDate.of(2024,3,15), "%Y-%m-%d"));
    }
    @Test public void formatMonthName() {
        assertEquals("March", DateTimeUtils.formatDate(LocalDate.of(2024,3,15), "%B"));
    }
    @Test public void formatShortMonth() {
        assertEquals("Mar", DateTimeUtils.formatDate(LocalDate.of(2024,3,15), "%b"));
    }
    @Test public void formatDayOfWeek() {
        assertEquals("Friday", DateTimeUtils.formatDate(LocalDate.of(2024,3,15), "%A"));
    }
    @Test public void formatTwoDigitYear() {
        assertEquals("24", DateTimeUtils.formatDate(LocalDate.of(2024,3,15), "%y"));
    }
}
