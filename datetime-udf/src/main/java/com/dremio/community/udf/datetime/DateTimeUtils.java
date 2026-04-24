package com.dremio.community.udf.datetime;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

public final class DateTimeUtils {

    private DateTimeUtils() {}

    // ── Epoch conversions ────────────────────────────────────────────────────

    public static LocalDate toLocalDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneOffset.UTC).toLocalDate();
    }

    public static long toEpochMillis(LocalDate d) {
        return d.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    // ── Fiscal calendar ──────────────────────────────────────────────────────
    //
    // Convention: when startMonth > 1, the fiscal year that begins in startMonth
    // of calendar year Y is named FY(Y+1).  For example, with startMonth=7:
    //   2023-07-01 → FY2024   |   2024-01-15 → FY2024   |   2024-07-01 → FY2025

    public static int fiscalYear(LocalDate date, int startMonth) {
        if (startMonth <= 1) return date.getYear();
        return date.getMonthValue() >= startMonth ? date.getYear() + 1 : date.getYear();
    }

    public static int fiscalQuarter(LocalDate date, int startMonth) {
        return (fiscalMonth(date, startMonth) - 1) / 3 + 1;
    }

    public static int fiscalMonth(LocalDate date, int startMonth) {
        int sm = Math.max(1, Math.min(12, startMonth));
        int fm = date.getMonthValue() - sm + 1;
        return fm <= 0 ? fm + 12 : fm;
    }

    public static int fiscalWeek(LocalDate date, int startMonth) {
        LocalDate fyStart = fiscalYearStart(date, startMonth);
        long days = ChronoUnit.DAYS.between(fyStart, date);
        return (int)(days / 7) + 1;
    }

    public static LocalDate fiscalYearStart(LocalDate date, int startMonth) {
        if (startMonth <= 1) return LocalDate.of(date.getYear(), 1, 1);
        int fy = fiscalYear(date, startMonth);
        return LocalDate.of(fy - 1, startMonth, 1);
    }

    public static LocalDate fiscalYearEnd(LocalDate date, int startMonth) {
        return fiscalYearStart(date, startMonth).plusYears(1).minusDays(1);
    }

    public static LocalDate fiscalQuarterStart(LocalDate date, int startMonth) {
        LocalDate fyStart = fiscalYearStart(date, startMonth);
        int fq = fiscalQuarter(date, startMonth);
        return fyStart.plusMonths((fq - 1) * 3L);
    }

    public static LocalDate fiscalQuarterEnd(LocalDate date, int startMonth) {
        return fiscalQuarterStart(date, startMonth).plusMonths(3).minusDays(1);
    }

    // ── Business days (weekday math only, no holiday calendars) ─────────────

    public static boolean isWeekday(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }

    /** Count weekdays in [start, end) — negative if start is after end. */
    public static int bizdaysBetween(LocalDate start, LocalDate end) {
        if (start.equals(end)) return 0;
        boolean negative = start.isAfter(end);
        if (negative) { LocalDate t = start; start = end; end = t; }

        long totalDays = ChronoUnit.DAYS.between(start, end);
        long weeks = totalDays / 7;
        int extra = (int)(totalDays % 7);
        int bizdays = (int)(weeks * 5);

        int startVal = start.getDayOfWeek().getValue(); // 1=Mon … 7=Sun
        for (int i = 0; i < extra; i++) {
            int dv = ((startVal - 1 + i) % 7) + 1;
            if (dv != 6 && dv != 7) bizdays++; // 6=Sat, 7=Sun
        }
        return negative ? -bizdays : bizdays;
    }

    public static LocalDate addBizdays(LocalDate date, int n) {
        if (n == 0) return date;
        int direction = n > 0 ? 1 : -1;
        int remaining = Math.abs(n);
        // Jump over full weeks for efficiency
        int fullWeeks = remaining / 5;
        remaining = remaining % 5;
        LocalDate d = date.plusWeeks(fullWeeks * direction);
        while (remaining > 0) {
            d = d.plusDays(direction);
            if (isWeekday(d)) remaining--;
        }
        return d;
    }

    public static LocalDate nextWeekday(LocalDate date) {
        LocalDate d = date.plusDays(1);
        while (!isWeekday(d)) d = d.plusDays(1);
        return d;
    }

    public static LocalDate prevWeekday(LocalDate date) {
        LocalDate d = date.minusDays(1);
        while (!isWeekday(d)) d = d.minusDays(1);
        return d;
    }

    // ── Period end boundaries ────────────────────────────────────────────────

    public static LocalDate weekEnd(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    public static LocalDate quarterEnd(LocalDate date) {
        int endMonth = ((date.getMonthValue() - 1) / 3 + 1) * 3;
        return LocalDate.of(date.getYear(), endMonth, 1)
                        .with(TemporalAdjusters.lastDayOfMonth());
    }

    public static LocalDate yearEnd(LocalDate date) {
        return LocalDate.of(date.getYear(), 12, 31);
    }

    // ── Date arithmetic ──────────────────────────────────────────────────────

    public static int diffMonths(LocalDate start, LocalDate end) {
        return (int) ChronoUnit.MONTHS.between(start, end);
    }

    public static int diffYears(LocalDate start, LocalDate end) {
        return (int) ChronoUnit.YEARS.between(start, end);
    }

    /** Age in completed years as of asOf. */
    public static int ageYears(LocalDate birthDate, LocalDate asOf) {
        return Period.between(birthDate, asOf).getYears();
    }

    public static boolean isLeapYear(LocalDate date) {
        return date.isLeapYear();
    }

    public static int daysInMonth(LocalDate date) {
        return date.lengthOfMonth();
    }

    // ── Date formatting ──────────────────────────────────────────────────────

    /**
     * Format a date using strftime-style tokens (%Y %m %d %A %B %b %a %j %W).
     * Falls back gracefully for unrecognised tokens.
     */
    public static String formatDate(LocalDate date, String pattern) {
        String p = pattern
            .replace("%Y", "yyyy")
            .replace("%y", "yy")
            .replace("%m", "MM")
            .replace("%d", "dd")
            .replace("%A", "EEEE")
            .replace("%a", "EEE")
            .replace("%B", "MMMM")
            .replace("%b", "MMM")
            .replace("%j", "DDD")
            .replace("%W", "ww")
            .replace("%e", "d");
        try {
            return date.format(DateTimeFormatter.ofPattern(p));
        } catch (Exception e) {
            return date.toString();
        }
    }
}
