package com.dremio.community.udf.json;

import org.junit.Test;
import static org.junit.Assert.*;

public class JsonUdfTest {

    static final String OBJ  = "{\"name\":\"alice\",\"age\":30,\"active\":true,\"score\":9.5}";
    static final String NESTED = "{\"user\":{\"id\":1,\"tags\":[\"a\",\"b\"]},\"count\":42}";
    static final String ARR   = "[\"x\",\"y\",\"z\"]";
    static final String NUM_ARR = "[1,2,3]";
    static final String EMPTY_OBJ = "{}";
    static final String INVALID = "not-json{";

    // ── Extraction ────────────────────────────────────────────────────────────

    @Test public void extractStr_topLevel() {
        assertEquals("alice", JsonUtils.extractStr(OBJ, "name"));
    }
    @Test public void extractStr_nested() {
        assertEquals("1", JsonUtils.extractStr(NESTED, "user.id"));
    }
    @Test public void extractStr_missing() {
        assertNull(JsonUtils.extractStr(OBJ, "missing"));
    }
    @Test public void extractStr_invalid() {
        assertNull(JsonUtils.extractStr(INVALID, "name"));
    }
    @Test public void extractStr_arrayIndex() {
        assertEquals("b", JsonUtils.extractStr(NESTED, "user.tags.1"));
    }
    @Test public void extractLong_integer() {
        assertEquals(Long.valueOf(30), JsonUtils.extractLong(OBJ, "age"));
    }
    @Test public void extractLong_nestedInteger() {
        assertEquals(Long.valueOf(42), JsonUtils.extractLong(NESTED, "count"));
    }
    @Test public void extractLong_notNumber() {
        assertNull(JsonUtils.extractLong(OBJ, "name"));
    }
    @Test public void extractLong_missing() {
        assertNull(JsonUtils.extractLong(OBJ, "missing"));
    }
    @Test public void extractDouble_float() {
        assertEquals(9.5, JsonUtils.extractDouble(OBJ, "score"), 0.0001);
    }
    @Test public void extractDouble_integer() {
        assertEquals(30.0, JsonUtils.extractDouble(OBJ, "age"), 0.0001);
    }
    @Test public void extractDouble_missing() {
        assertNull(JsonUtils.extractDouble(OBJ, "missing"));
    }
    @Test public void extractBool_true() {
        assertEquals(Integer.valueOf(1), JsonUtils.extractBool(OBJ, "active"));
    }
    @Test public void extractBool_notBoolean() {
        assertNull(JsonUtils.extractBool(OBJ, "name"));
    }
    @Test public void extractBool_missing() {
        assertNull(JsonUtils.extractBool(OBJ, "missing"));
    }
    @Test public void extractRaw_object() {
        String r = JsonUtils.extractRaw(NESTED, "user");
        assertNotNull(r);
        assertTrue(r.contains("\"id\":1"));
    }
    @Test public void extractRaw_array() {
        String r = JsonUtils.extractRaw(NESTED, "user.tags");
        assertNotNull(r);
        assertEquals("[\"a\",\"b\"]", r);
    }
    @Test public void extractRaw_missing() {
        assertNull(JsonUtils.extractRaw(OBJ, "missing"));
    }

    // ── Inspection ────────────────────────────────────────────────────────────

    @Test public void isValid_object() {
        assertTrue(JsonUtils.isValid(OBJ));
    }
    @Test public void isValid_array() {
        assertTrue(JsonUtils.isValid(ARR));
    }
    @Test public void isValid_invalid() {
        assertFalse(JsonUtils.isValid(INVALID));
    }
    @Test public void isValid_emptyString() {
        assertFalse(JsonUtils.isValid(""));
    }
    @Test public void type_object() {
        assertEquals("object", JsonUtils.type(OBJ));
    }
    @Test public void type_array() {
        assertEquals("array", JsonUtils.type(ARR));
    }
    @Test public void type_string() {
        assertEquals("string", JsonUtils.type("\"hello\""));
    }
    @Test public void type_number() {
        assertEquals("number", JsonUtils.type("42"));
    }
    @Test public void type_boolean() {
        assertEquals("boolean", JsonUtils.type("true"));
    }
    @Test public void type_null() {
        assertEquals("null", JsonUtils.type("null"));
    }
    @Test public void type_invalid() {
        assertEquals("invalid", JsonUtils.type(INVALID));
    }
    @Test public void length_object() {
        assertEquals(4L, JsonUtils.length(OBJ));
    }
    @Test public void length_array() {
        assertEquals(3L, JsonUtils.length(ARR));
    }
    @Test public void length_emptyObject() {
        assertEquals(0L, JsonUtils.length(EMPTY_OBJ));
    }
    @Test public void length_nonContainer() {
        assertEquals(-1L, JsonUtils.length("42"));
    }
    @Test public void hasKey_present() {
        assertEquals(1, JsonUtils.hasKey(OBJ, "name"));
    }
    @Test public void hasKey_missing() {
        assertEquals(0, JsonUtils.hasKey(OBJ, "missing"));
    }
    @Test public void hasKey_notObject() {
        assertEquals(0, JsonUtils.hasKey(ARR, "0"));
    }
    @Test public void keys_object() {
        String k = JsonUtils.keys(OBJ);
        assertNotNull(k);
        assertTrue(k.contains("name"));
        assertTrue(k.contains("age"));
        assertTrue(k.contains("active"));
    }
    @Test public void keys_notObject() {
        assertNull(JsonUtils.keys(ARR));
    }
    @Test public void keys_empty() {
        assertEquals("", JsonUtils.keys(EMPTY_OBJ));
    }

    // ── Manipulation ─────────────────────────────────────────────────────────

    @Test public void set_newKey() {
        String r = JsonUtils.set(OBJ, "city", "\"NYC\"");
        assertTrue(r.contains("\"city\":\"NYC\""));
    }
    @Test public void set_overwrite() {
        String r = JsonUtils.set(OBJ, "name", "\"bob\"");
        assertTrue(r.contains("\"name\":\"bob\""));
        assertFalse(r.contains("alice"));
    }
    @Test public void set_jsonValue() {
        String r = JsonUtils.set(EMPTY_OBJ, "nums", "[1,2,3]");
        assertTrue(r.contains("[1,2,3]"));
    }
    @Test public void set_rawStringFallback() {
        // 'hello' is not valid JSON — stored as string
        String r = JsonUtils.set(EMPTY_OBJ, "greet", "hello");
        assertTrue(r.contains("\"greet\":\"hello\""));
    }
    @Test public void delete_existing() {
        String r = JsonUtils.delete(OBJ, "age");
        assertFalse(r.contains("\"age\""));
        assertTrue(r.contains("\"name\""));
    }
    @Test public void delete_missing() {
        String r = JsonUtils.delete(OBJ, "missing");
        assertEquals(JsonUtils.minify(OBJ), r);
    }
    @Test public void merge_basic() {
        String r = JsonUtils.merge("{\"a\":1}", "{\"b\":2}");
        assertTrue(r.contains("\"a\":1"));
        assertTrue(r.contains("\"b\":2"));
    }
    @Test public void merge_overwrite() {
        String r = JsonUtils.merge("{\"a\":1}", "{\"a\":99}");
        assertTrue(r.contains("\"a\":99"));
        assertFalse(r.contains("\"a\":1,"));
    }
    @Test public void pretty_addsWhitespace() {
        String r = JsonUtils.pretty(OBJ);
        assertTrue(r.contains("\n"));
        assertTrue(r.contains("  "));
    }
    @Test public void minify_removesWhitespace() {
        String pretty = JsonUtils.pretty(OBJ);
        String r = JsonUtils.minify(pretty);
        assertFalse(r.contains("\n"));
        assertFalse(r.contains("  "));
        assertEquals(JsonUtils.minify(OBJ), r);
    }

    // ── Array operations ─────────────────────────────────────────────────────

    @Test public void arrayLength_array() {
        assertEquals(3L, JsonUtils.arrayLength(ARR));
    }
    @Test public void arrayLength_notArray() {
        assertEquals(-1L, JsonUtils.arrayLength(OBJ));
    }
    @Test public void arrayLength_empty() {
        assertEquals(0L, JsonUtils.arrayLength("[]"));
    }
    @Test public void arrayGet_first() {
        assertEquals("x", JsonUtils.arrayGet(ARR, 0));
    }
    @Test public void arrayGet_last() {
        assertEquals("z", JsonUtils.arrayGet(ARR, 2));
    }
    @Test public void arrayGet_outOfBounds() {
        assertNull(JsonUtils.arrayGet(ARR, 99));
    }
    @Test public void arrayGet_numericArr() {
        assertEquals("2", JsonUtils.arrayGet(NUM_ARR, 1));
    }
    @Test public void arrayContainsStr_found() {
        assertEquals(1, JsonUtils.arrayContainsStr(ARR, "y"));
    }
    @Test public void arrayContainsStr_notFound() {
        assertEquals(0, JsonUtils.arrayContainsStr(ARR, "q"));
    }
    @Test public void arrayContainsStr_notArray() {
        assertEquals(0, JsonUtils.arrayContainsStr(OBJ, "alice"));
    }
    @Test public void arrayAppend_string() {
        String r = JsonUtils.arrayAppend(ARR, "w");
        assertTrue(r.contains("\"w\""));
        assertEquals(4L, JsonUtils.arrayLength(r));
    }
    @Test public void arrayAppend_json() {
        String r = JsonUtils.arrayAppend(NUM_ARR, "4");
        assertEquals(4L, JsonUtils.arrayLength(r));
        assertEquals("4", JsonUtils.arrayGet(r, 3));
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    @Test public void fromKV_basic() {
        String r = JsonUtils.fromKV("city", "NYC");
        assertEquals("{\"city\":\"NYC\"}", r);
    }
    @Test public void fromKV_specialChars() {
        String r = JsonUtils.fromKV("msg", "hello world");
        assertEquals("{\"msg\":\"hello world\"}", r);
    }
    @Test public void wrapStr_basic() {
        assertEquals("\"hello\"", JsonUtils.wrapStr("hello"));
    }
    @Test public void wrapStr_escapesQuotes() {
        String r = JsonUtils.wrapStr("say \"hi\"");
        assertTrue(r.startsWith("\""));
        assertTrue(r.contains("\\\""));
    }
    @Test public void wrapStr_empty() {
        assertEquals("\"\"", JsonUtils.wrapStr(""));
    }
}
