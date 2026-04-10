package com.dremio.community.udf.pii;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Static utility methods for PII detection, masking, extraction, and hashing.
 *
 * All methods are static and thread-safe.  Regex patterns are compiled once at
 * class-load time and reused across rows.  No external dependencies — pure Java.
 *
 * Called from Dremio UDF eval() bodies using fully-qualified class names.
 */
public final class PiiUtils {

  private PiiUtils() {}

  // ── Compiled Patterns (class-load time — compiled once, reused per row) ──────

  // EMAIL — anchored for detection, unanchored for extraction
  private static final Pattern P_EMAIL =
      Pattern.compile("^[a-zA-Z0-9._%+\\-]{1,64}@[a-zA-Z0-9.\\-]{1,253}\\.[a-zA-Z]{2,}$",
          Pattern.CASE_INSENSITIVE);
  static final Pattern P_EMAIL_FIND =
      Pattern.compile("[a-zA-Z0-9._%+\\-]{1,64}@[a-zA-Z0-9.\\-]{1,253}\\.[a-zA-Z]{2,}",
          Pattern.CASE_INSENSITIVE);

  // PHONE — NANP + international
  private static final Pattern P_PHONE =
      Pattern.compile("^(\\+?1[\\s\\-.])?(\\(?[2-9][0-9]{2}\\)?[\\s\\-.]?[2-9][0-9]{2}[\\s\\-.]?[0-9]{4}|\\+[1-9][0-9]{0,2}[\\s\\-.]?[0-9]{4,14})$");
  static final Pattern P_PHONE_FIND =
      Pattern.compile("(\\+?1[\\s\\-.])?(\\(?[2-9][0-9]{2}\\)?[\\s\\-.]?[2-9][0-9]{2}[\\s\\-.]?[0-9]{4}|\\+[1-9][0-9]{0,2}[\\s\\-.]?[0-9]{4,14})");

  // SSN — excludes invalid ranges
  private static final Pattern P_SSN =
      Pattern.compile("^(?!000|666|9[0-9]{2})[0-9]{3}[\\- ]?(?!00)[0-9]{2}[\\- ]?(?!0000)[0-9]{4}$");
  static final Pattern P_SSN_FIND =
      Pattern.compile("(?<![0-9])(?!000|666|9[0-9]{2})[0-9]{3}[\\- ]?(?!00)[0-9]{2}[\\- ]?(?!0000)[0-9]{4}(?![0-9])");

  // CREDIT CARD — Visa, MC, Amex, Discover; separators allowed
  private static final Pattern P_CC =
      Pattern.compile("^(?:4[0-9]{3}|5[1-5][0-9]{2}|2(?:2[2-9][1-9]|[3-6][0-9]{2}|7[01][0-9]|720)[0-9]?|3[47][0-9]{2}|6(?:011|5[0-9]{2}))[\\s\\-]?[0-9]{4}[\\s\\-]?[0-9]{4}[\\s\\-]?[0-9]{1,7}$");
  static final Pattern P_CC_FIND =
      Pattern.compile("(?:4[0-9]{3}|5[1-5][0-9]{2}|3[47][0-9]{2}|6(?:011|5[0-9]{2}))[\\s\\-]?[0-9]{4}[\\s\\-]?[0-9]{4}[\\s\\-]?[0-9]{1,7}");

  // IBAN
  private static final Pattern P_IBAN =
      Pattern.compile("^[A-Z]{2}[0-9]{2}[A-Z0-9]{4,30}$", Pattern.CASE_INSENSITIVE);
  static final Pattern P_IBAN_FIND =
      Pattern.compile("[A-Z]{2}[0-9]{2}[A-Z0-9]{4,30}", Pattern.CASE_INSENSITIVE);

  // IPv4
  private static final Pattern P_IPV4 =
      Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])$");
  static final Pattern P_IPV4_FIND =
      Pattern.compile("\\b(?:(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])\\b");

  // IPv6 — full and compressed forms
  private static final Pattern P_IPV6 =
      Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$" +
          "|^(?:[0-9a-fA-F]{1,4}:){1,7}:$" +
          "|^:(?::[0-9a-fA-F]{1,4}){1,7}$" +
          "|^(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$" +
          "|^(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}$" +
          "|^(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}$" +
          "|^(?:[0-9a-fA-F]{1,4}:){1,3}(?::[0-9a-fA-F]{1,4}){1,4}$" +
          "|^(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}$" +
          "|^::(?:[fF]{4}(?::0{1,4})?:)?(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$" +
          "|^::$");
  static final Pattern P_IPV6_FIND =
      Pattern.compile("(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}" +
          "|(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}" +
          "|::(?:[0-9a-fA-F]{1,4}:){0,5}[0-9a-fA-F]{1,4}");

  // US ZIP
  private static final Pattern P_ZIP =
      Pattern.compile("^[0-9]{5}(?:-[0-9]{4})?$");

  // DATE OF BIRTH — ISO and US formats
  private static final Pattern P_DOB =
      Pattern.compile("^(?:(19|20)[0-9]{2}[\\-/](0[1-9]|1[0-2])[\\-/](0[1-9]|[12][0-9]|3[01])" +
          "|(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/(19|20)[0-9]{2})$");

  // PASSPORT — US 9 chars, generic 6-9 alphanumeric
  private static final Pattern P_PASSPORT =
      Pattern.compile("^(?:[A-Z][0-9]{8}|[A-Z]{1,2}[0-9]{6,7}|[A-Z0-9]{8,9})$",
          Pattern.CASE_INSENSITIVE);

  // VIN — 17 chars, excludes I, O, Q
  private static final Pattern P_VIN =
      Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$", Pattern.CASE_INSENSITIVE);

  // NPI — 10 digits
  private static final Pattern P_NPI =
      Pattern.compile("^[0-9]{10}$");

  // EIN — XX-XXXXXXX
  private static final Pattern P_EIN =
      Pattern.compile("^([0-9]{2})-([0-9]{7})$");

  // MAC Address — colon, hyphen, dot, or bare formats
  private static final Pattern P_MAC =
      Pattern.compile("^(?:[0-9a-fA-F]{2}[:\\-]){5}[0-9a-fA-F]{2}$" +
          "|^[0-9a-fA-F]{12}$" +
          "|^(?:[0-9a-fA-F]{4}\\.){2}[0-9a-fA-F]{4}$");

  // URL
  private static final Pattern P_URL =
      Pattern.compile("^https?://[a-zA-Z0-9\\-._~:/?#\\[\\]@!$&'()*+,;=%]{3,2048}$",
          Pattern.CASE_INSENSITIVE);
  static final Pattern P_URL_FIND =
      Pattern.compile("https?://[a-zA-Z0-9\\-._~:/?#\\[\\]@!$&'()*+,;=%]{3,2048}",
          Pattern.CASE_INSENSITIVE);

  // Valid EIN prefixes (IRS campus codes)
  private static final java.util.Set<Integer> VALID_EIN_PREFIXES = buildEinPrefixes();
  private static java.util.Set<Integer> buildEinPrefixes() {
    java.util.Set<Integer> s = new java.util.HashSet<>();
    int[][] ranges = {{1,6},{10,16},{20,27},{30,39},{40,48},{50,68},{71,77},{80,88},{90,98},{99,99}};
    for (int[] r : ranges) for (int i = r[0]; i <= r[1]; i++) s.add(i);
    return java.util.Collections.unmodifiableSet(s);
  }

  // VIN transliteration table
  private static final int[] VIN_VALUES = new int[128];
  private static final int[] VIN_WEIGHTS = {8,7,6,5,4,3,2,10,0,9,8,7,6,5,4,3,2};
  static {
    String chars = "0123456789.ABCDEFGH..JKLMN.P.R..STUVWXYZ";
    for (int i = 0; i < chars.length(); i++) {
      if (chars.charAt(i) != '.') VIN_VALUES[chars.charAt(i)] = i % 10 == 0 ? 0 : i;
    }
    // Direct mapping
    for (char c = '0'; c <= '9'; c++) VIN_VALUES[c] = c - '0';
    VIN_VALUES['A'] = 1; VIN_VALUES['B'] = 2; VIN_VALUES['C'] = 3; VIN_VALUES['D'] = 4;
    VIN_VALUES['E'] = 5; VIN_VALUES['F'] = 6; VIN_VALUES['G'] = 7; VIN_VALUES['H'] = 8;
    VIN_VALUES['J'] = 1; VIN_VALUES['K'] = 2; VIN_VALUES['L'] = 3; VIN_VALUES['M'] = 4;
    VIN_VALUES['N'] = 5; VIN_VALUES['P'] = 7; VIN_VALUES['R'] = 9;
    VIN_VALUES['S'] = 2; VIN_VALUES['T'] = 3; VIN_VALUES['U'] = 4; VIN_VALUES['V'] = 5;
    VIN_VALUES['W'] = 6; VIN_VALUES['X'] = 7; VIN_VALUES['Y'] = 8; VIN_VALUES['Z'] = 9;
  }

  // ── Detection ──────────────────────────────────────────────────────────────

  public static boolean isEmail(String s) {
    if (s == null || s.isEmpty()) return false;
    return P_EMAIL.matcher(s.trim()).matches();
  }

  public static boolean isPhone(String s) {
    if (s == null || s.isEmpty()) return false;
    String t = s.trim();
    // Must have at least 7 digits
    int digits = 0;
    for (char c : t.toCharArray()) if (Character.isDigit(c)) digits++;
    if (digits < 7 || digits > 15) return false;
    return P_PHONE.matcher(t).matches();
  }

  public static boolean isSsn(String s) {
    if (s == null || s.isEmpty()) return false;
    return P_SSN.matcher(s.trim()).matches() && isValidSsnStr(s.trim());
  }

  public static boolean isCreditCard(String s) {
    if (s == null || s.isEmpty()) return false;
    String t = s.trim();
    if (!P_CC.matcher(t).matches()) return false;
    return luhn(t.replaceAll("[\\s\\-]", ""));
  }

  public static boolean isIban(String s) {
    if (s == null || s.isEmpty()) return false;
    String t = s.trim().replaceAll("\\s+", "").toUpperCase();
    if (!P_IBAN.matcher(t).matches()) return false;
    return ibanMod97(t);
  }

  public static boolean isIpv4(String s) {
    if (s == null || s.isEmpty()) return false;
    return P_IPV4.matcher(s.trim()).matches();
  }

  public static boolean isIpv6(String s) {
    if (s == null || s.isEmpty()) return false;
    return P_IPV6.matcher(s.trim()).matches();
  }

  public static boolean isUsZip(String s) {
    if (s == null || s.isEmpty()) return false;
    return P_ZIP.matcher(s.trim()).matches();
  }

  public static boolean isDateOfBirth(String s) {
    if (s == null || s.isEmpty()) return false;
    Matcher m = P_DOB.matcher(s.trim());
    if (!m.matches()) return false;
    // Calendar validation
    try {
      String t = s.trim().replaceAll("[/\\-]", "-");
      String[] parts = t.split("-");
      if (parts.length == 3) {
        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        int c = Integer.parseInt(parts[2]);
        // ISO: YYYY-MM-DD
        if (a >= 1900) return isValidDate(a, b, c);
        // US: MM-DD-YYYY
        return isValidDate(c, a, b);
      }
    } catch (NumberFormatException e) { /* fall through */ }
    return true;
  }

  public static boolean isPassport(String s) {
    if (s == null || s.isEmpty()) return false;
    return P_PASSPORT.matcher(s.trim()).matches();
  }

  public static boolean isVin(String s) {
    if (s == null || s.isEmpty()) return false;
    String t = s.trim().toUpperCase();
    if (!P_VIN.matcher(t).matches()) return false;
    return isValidVinCheckDigit(t);
  }

  public static boolean isNpi(String s) {
    if (s == null || s.isEmpty()) return false;
    String t = s.trim();
    if (!P_NPI.matcher(t).matches()) return false;
    return isNpiLuhn(t);
  }

  public static boolean isEin(String s) {
    if (s == null || s.isEmpty()) return false;
    Matcher m = P_EIN.matcher(s.trim());
    if (!m.matches()) return false;
    int prefix = Integer.parseInt(m.group(1));
    return VALID_EIN_PREFIXES.contains(prefix);
  }

  public static boolean isMacAddress(String s) {
    if (s == null || s.isEmpty()) return false;
    return P_MAC.matcher(s.trim()).matches();
  }

  public static boolean isUrl(String s) {
    if (s == null || s.isEmpty()) return false;
    return P_URL.matcher(s.trim()).matches();
  }

  public static boolean isPii(String s) {
    if (s == null || s.isEmpty()) return false;
    return isEmail(s) || isPhone(s) || isSsn(s) || isCreditCard(s) ||
           isIban(s) || isIpv4(s) || isIpv6(s) || isPassport(s) ||
           isVin(s) || isNpi(s) || isEin(s) || isMacAddress(s) || isUrl(s);
  }

  /** Returns first detected PII type name, or null if none. */
  public static String piiType(String s) {
    if (s == null || s.isEmpty()) return null;
    if (isEmail(s))       return "EMAIL";
    if (isSsn(s))         return "SSN";
    if (isCreditCard(s))  return "CREDIT_CARD";
    if (isIban(s))        return "IBAN";
    if (isPhone(s))       return "PHONE";
    if (isIpv4(s))        return "IPV4";
    if (isIpv6(s))        return "IPV6";
    if (isUsZip(s))       return "US_ZIP";
    if (isEin(s))         return "EIN";
    if (isNpi(s))         return "NPI";
    if (isVin(s))         return "VIN";
    if (isMacAddress(s))  return "MAC_ADDRESS";
    if (isPassport(s))    return "PASSPORT";
    if (isDateOfBirth(s)) return "DATE_OF_BIRTH";
    if (isUrl(s))         return "URL";
    return null;
  }

  /** Returns count of distinct PII types detected in value. */
  public static int piiScore(String s) {
    if (s == null || s.isEmpty()) return 0;
    int score = 0;
    if (isEmail(s))       score++;
    if (isSsn(s))         score++;
    if (isCreditCard(s))  score++;
    if (isIban(s))        score++;
    if (isPhone(s))       score++;
    if (isIpv4(s))        score++;
    if (isIpv6(s))        score++;
    if (isEin(s))         score++;
    if (isNpi(s))         score++;
    if (isVin(s))         score++;
    if (isMacAddress(s))  score++;
    if (isPassport(s))    score++;
    if (isDateOfBirth(s)) score++;
    if (isUrl(s))         score++;
    return score;
  }

  // ── Validation Primitives ──────────────────────────────────────────────────

  /** Luhn algorithm — credit cards, NPI variant. */
  public static boolean luhn(String digits) {
    if (digits == null) return false;
    String d = digits.replaceAll("[^0-9]", "");
    int len = d.length();
    if (len < 12 || len > 19) return false;
    int sum = 0;
    boolean alternate = false;
    for (int i = len - 1; i >= 0; i--) {
      int n = d.charAt(i) - '0';
      if (alternate) { n *= 2; if (n > 9) n -= 9; }
      sum += n;
      alternate = !alternate;
    }
    return sum % 10 == 0;
  }

  /** IBAN mod-97 checksum validation. */
  public static boolean ibanMod97(String raw) {
    if (raw == null || raw.length() < 15 || raw.length() > 34) return false;
    String rearranged = raw.substring(4) + raw.substring(0, 4);
    long acc = 0;
    for (int i = 0; i < rearranged.length(); i++) {
      char c = rearranged.charAt(i);
      if (c >= '0' && c <= '9') {
        acc = (acc * 10 + (c - '0')) % 97;
      } else if (c >= 'A' && c <= 'Z') {
        int val = c - 'A' + 10;
        acc = (acc * 10 + val / 10) % 97;
        acc = (acc * 10 + val % 10) % 97;
      } else {
        return false;
      }
    }
    return acc == 1;
  }

  /** SSN range validation after regex match. */
  private static boolean isValidSsnStr(String s) {
    String digits = s.replaceAll("[^0-9]", "");
    if (digits.length() != 9) return false;
    int area = Integer.parseInt(digits.substring(0, 3));
    int group = Integer.parseInt(digits.substring(3, 5));
    int serial = Integer.parseInt(digits.substring(5));
    return area != 0 && area != 666 && area < 900 && group != 0 && serial != 0;
  }

  /** VIN check digit validation. */
  public static boolean isValidVinCheckDigit(String vin) {
    if (vin == null || vin.length() != 17) return false;
    String v = vin.toUpperCase();
    int sum = 0;
    for (int i = 0; i < 17; i++) {
      char c = v.charAt(i);
      if (c < 0 || c >= VIN_VALUES.length) return false;
      sum += VIN_VALUES[c] * VIN_WEIGHTS[i];
    }
    int remainder = sum % 11;
    char expected = remainder == 10 ? 'X' : (char) ('0' + remainder);
    return v.charAt(8) == expected;
  }

  /** NPI Luhn variant — prepend 80840 then run Luhn on 15-char string. */
  public static boolean isNpiLuhn(String npi) {
    if (npi == null || npi.length() != 10) return false;
    return luhn("80840" + npi);
  }

  public static boolean isValidDate(int y, int m, int d) {
    if (m < 1 || m > 12 || d < 1) return false;
    int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    if (isLeapYear(y)) days[1] = 29;
    return d <= days[m - 1];
  }

  public static boolean isLeapYear(int y) {
    return (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0);
  }

  // ── Masking ────────────────────────────────────────────────────────────────

  public static String maskEmail(String s, char mc) {
    if (s == null) return null;
    int at = s.indexOf('@');
    if (at <= 0) return maskCustom(s, 1, 0, mc);
    String local = s.substring(0, at);
    String domain = s.substring(at); // includes @
    String maskedLocal;
    if (local.length() <= 1) {
      maskedLocal = local;
    } else {
      maskedLocal = local.charAt(0) + repeat(mc, local.length() - 1);
    }
    return maskedLocal + domain;
  }

  public static String maskPhone(String s) {
    if (s == null) return null;
    // Extract only digits
    StringBuilder digits = new StringBuilder();
    for (char c : s.toCharArray()) if (Character.isDigit(c)) digits.append(c);
    String d = digits.toString();
    int len = d.length();
    if (len < 7) return repeat('*', len);
    // Show last 4 digits
    String last4 = d.substring(len - 4);
    if (len == 10) return "***-***-" + last4;
    if (len == 11) return "+*-***-***-" + last4;
    return repeat('*', len - 4) + last4;
  }

  public static String maskSsn(String s) {
    if (s == null) return null;
    String digits = s.replaceAll("[^0-9]", "");
    if (digits.length() != 9) return repeat('*', s.length());
    return "***-**-" + digits.substring(5);
  }

  public static String maskCreditCard(String s) {
    if (s == null) return null;
    String digits = s.replaceAll("[^0-9]", "");
    if (digits.length() < 13) return repeat('*', s.length());
    String last4 = digits.substring(digits.length() - 4);
    return "****-****-****-" + last4;
  }

  public static String maskCreditCardFull(String s) {
    if (s == null) return null;
    return "[REDACTED CARD]";
  }

  public static String maskIban(String s) {
    if (s == null) return null;
    String t = s.trim().replaceAll("\\s+", "").toUpperCase();
    if (t.length() < 6) return repeat('*', t.length());
    String country = t.substring(0, 2);
    String check = t.substring(2, 4);
    String last4 = t.length() >= 4 ? t.substring(t.length() - 4) : "";
    int maskLen = t.length() - 8;
    return country + check + repeat('*', Math.max(0, maskLen)) + last4;
  }

  public static String maskIpv4(String s, int maskOctets) {
    if (s == null) return null;
    String[] parts = s.trim().split("\\.");
    if (parts.length != 4) return s;
    int keep = 4 - Math.min(4, Math.max(0, maskOctets));
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      if (i > 0) sb.append('.');
      sb.append(i < keep ? parts[i] : "*");
    }
    return sb.toString();
  }

  public static String maskName(String s) {
    if (s == null || s.isEmpty()) return s;
    String[] words = s.split("\\s+");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < words.length; i++) {
      if (i > 0) sb.append(' ');
      String w = words[i];
      if (w.isEmpty()) continue;
      sb.append(w.charAt(0));
      if (w.length() > 1) sb.append(repeat('*', w.length() - 1));
    }
    return sb.toString();
  }

  public static String maskDateOfBirth(String s) {
    if (s == null || s.isEmpty()) return s;
    // Mask the year — detect position by length and format
    String t = s.trim();
    // ISO: YYYY-MM-DD → ****-MM-DD
    if (t.matches("\\d{4}[\\-/]\\d{2}[\\-/]\\d{2}")) {
      return "****" + t.substring(4);
    }
    // US: MM/DD/YYYY → MM/DD/****
    if (t.matches("\\d{2}/\\d{2}/\\d{4}")) {
      return t.substring(0, 6) + "****";
    }
    return repeat('*', t.length());
  }

  public static String maskCustom(String s, int keepLeft, int keepRight, char mc) {
    if (s == null) return null;
    int len = s.length();
    if (keepLeft + keepRight >= len) return s;
    String left  = s.substring(0, keepLeft);
    String right = s.substring(len - keepRight);
    int maskLen  = len - keepLeft - keepRight;
    return left + repeat(mc, maskLen) + right;
  }

  public static String redact(String s) {
    return "[REDACTED]";
  }

  // ── Extraction ─────────────────────────────────────────────────────────────

  public static String extractEmail(String s) {
    if (s == null) return null;
    Matcher m = P_EMAIL_FIND.matcher(s);
    return m.find() ? m.group() : null;
  }

  public static String extractPhone(String s) {
    if (s == null) return null;
    Matcher m = P_PHONE_FIND.matcher(s);
    return m.find() ? m.group() : null;
  }

  public static String extractSsn(String s) {
    if (s == null) return null;
    Matcher m = P_SSN_FIND.matcher(s);
    while (m.find()) {
      String candidate = m.group();
      if (isValidSsnStr(candidate)) return candidate;
    }
    return null;
  }

  public static String extractCreditCard(String s) {
    if (s == null) return null;
    Matcher m = P_CC_FIND.matcher(s);
    while (m.find()) {
      String candidate = m.group();
      if (luhn(candidate.replaceAll("[\\s\\-]", ""))) return candidate;
    }
    return null;
  }

  public static String extractIpv4(String s) {
    if (s == null) return null;
    Matcher m = P_IPV4_FIND.matcher(s);
    return m.find() ? m.group() : null;
  }

  public static String extractIpv6(String s) {
    if (s == null) return null;
    Matcher m = P_IPV6_FIND.matcher(s);
    return m.find() ? m.group() : null;
  }

  public static String extractIban(String s) {
    if (s == null) return null;
    Matcher m = P_IBAN_FIND.matcher(s);
    while (m.find()) {
      String candidate = m.group().replaceAll("\\s+", "").toUpperCase();
      if (ibanMod97(candidate)) return candidate;
    }
    return null;
  }

  public static String extractUrl(String s) {
    if (s == null) return null;
    Matcher m = P_URL_FIND.matcher(s);
    return m.find() ? m.group() : null;
  }

  /** Returns JSON array of detected PII types and values, or null if none found. */
  public static String extractAllPii(String s) {
    if (s == null || s.isEmpty()) return null;
    StringBuilder sb = new StringBuilder("[");
    boolean found = false;

    String email = extractEmail(s);
    if (email != null) { if (found) sb.append(","); sb.append("{\"type\":\"email\",\"value\":\"").append(jsonEscape(email)).append("\"}"); found = true; }

    String phone = extractPhone(s);
    if (phone != null) { if (found) sb.append(","); sb.append("{\"type\":\"phone\",\"value\":\"").append(jsonEscape(phone)).append("\"}"); found = true; }

    String ssn = extractSsn(s);
    if (ssn != null) { if (found) sb.append(","); sb.append("{\"type\":\"ssn\",\"value\":\"").append(jsonEscape(ssn)).append("\"}"); found = true; }

    String cc = extractCreditCard(s);
    if (cc != null) { if (found) sb.append(","); sb.append("{\"type\":\"credit_card\",\"value\":\"").append(jsonEscape(cc)).append("\"}"); found = true; }

    String ip4 = extractIpv4(s);
    if (ip4 != null) { if (found) sb.append(","); sb.append("{\"type\":\"ipv4\",\"value\":\"").append(jsonEscape(ip4)).append("\"}"); found = true; }

    String ip6 = extractIpv6(s);
    if (ip6 != null) { if (found) sb.append(","); sb.append("{\"type\":\"ipv6\",\"value\":\"").append(jsonEscape(ip6)).append("\"}"); found = true; }

    String url = extractUrl(s);
    if (url != null) { if (found) sb.append(","); sb.append("{\"type\":\"url\",\"value\":\"").append(jsonEscape(url)).append("\"}"); found = true; }

    if (!found) return null;
    sb.append("]");
    return sb.toString();
  }

  // ── Hashing / Tokenization ─────────────────────────────────────────────────

  public static String sha256Hex(String s) {
    if (s == null) return null;
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      return toHex(hash);
    } catch (java.security.NoSuchAlgorithmException e) {
      return null;
    }
  }

  public static String sha256Hex(String s, String salt) {
    if (s == null) return null;
    return sha256Hex((salt == null ? "" : salt) + s);
  }

  public static String md5Hex(String s) {
    if (s == null) return null;
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
      byte[] hash = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      return toHex(hash);
    } catch (java.security.NoSuchAlgorithmException e) {
      return null;
    }
  }

  public static String tokenize(String s) {
    if (s == null) return null;
    String hex = sha256Hex(s);
    if (hex == null) return null;
    return "TOK-" + hex.substring(0, 16).toUpperCase();
  }

  // ── Internal Helpers ───────────────────────────────────────────────────────

  private static String repeat(char c, int n) {
    if (n <= 0) return "";
    char[] arr = new char[n];
    java.util.Arrays.fill(arr, c);
    return new String(arr);
  }

  private static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) sb.append(String.format("%02x", b & 0xFF));
    return sb.toString();
  }

  private static String jsonEscape(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
