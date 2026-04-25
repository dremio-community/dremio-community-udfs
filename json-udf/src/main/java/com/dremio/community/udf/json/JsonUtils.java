/*
 * Dremio JSON UDF — Core utility class
 * All logic lives here; UDF eval() methods call these static methods.
 * Uses Jackson ObjectMapper (bundled in Dremio — no extra dependency at runtime).
 */
package com.dremio.community.udf.json;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class JsonUtils {

    static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {}

    // Converts dot-notation path ("a.b.c" or "a.0.b") to JsonPointer ("/a/b/c")
    static JsonPointer toPointer(String path) {
        if (path == null || path.isEmpty()) return JsonPointer.compile("");
        return JsonPointer.compile("/" + path.replace('.', '/'));
    }

    // ── Extraction ────────────────────────────────────────────────────────────

    public static String extractStr(String json, String path) {
        try {
            JsonNode node = MAPPER.readTree(json).at(toPointer(path));
            if (node.isMissingNode() || node.isNull()) return null;
            return node.isTextual() ? node.asText() : node.toString();
        } catch (Exception e) { return null; }
    }

    public static Long extractLong(String json, String path) {
        try {
            JsonNode node = MAPPER.readTree(json).at(toPointer(path));
            if (node.isMissingNode() || node.isNull() || !node.isNumber()) return null;
            return node.longValue();
        } catch (Exception e) { return null; }
    }

    public static Double extractDouble(String json, String path) {
        try {
            JsonNode node = MAPPER.readTree(json).at(toPointer(path));
            if (node.isMissingNode() || node.isNull() || !node.isNumber()) return null;
            return node.doubleValue();
        } catch (Exception e) { return null; }
    }

    // Returns 1 for true, 0 for false, null if missing/not-boolean
    public static Integer extractBool(String json, String path) {
        try {
            JsonNode node = MAPPER.readTree(json).at(toPointer(path));
            if (node.isMissingNode() || node.isNull() || !node.isBoolean()) return null;
            return node.booleanValue() ? 1 : 0;
        } catch (Exception e) { return null; }
    }

    // Returns raw JSON string for the node (objects/arrays preserved as JSON)
    public static String extractRaw(String json, String path) {
        try {
            JsonNode node = MAPPER.readTree(json).at(toPointer(path));
            if (node.isMissingNode() || node.isNull()) return null;
            return node.toString();
        } catch (Exception e) { return null; }
    }

    // ── Inspection ────────────────────────────────────────────────────────────

    public static boolean isValid(String json) {
        try { MAPPER.readTree(json); return true; }
        catch (Exception e) { return false; }
    }

    public static String type(String json) {
        try {
            JsonNode node = MAPPER.readTree(json);
            if (node.isObject())  return "object";
            if (node.isArray())   return "array";
            if (node.isTextual()) return "string";
            if (node.isNumber())  return "number";
            if (node.isBoolean()) return "boolean";
            if (node.isNull())    return "null";
            return "unknown";
        } catch (Exception e) { return "invalid"; }
    }

    // Object: key count; Array: element count; other: -1
    public static long length(String json) {
        try {
            JsonNode node = MAPPER.readTree(json);
            if (node.isObject() || node.isArray()) return node.size();
            return -1L;
        } catch (Exception e) { return -1L; }
    }

    // Returns 1 if top-level key exists in object, 0 otherwise
    public static int hasKey(String json, String key) {
        try {
            JsonNode node = MAPPER.readTree(json);
            return (node.isObject() && node.has(key)) ? 1 : 0;
        } catch (Exception e) { return 0; }
    }

    // Returns comma-separated top-level keys of a JSON object
    public static String keys(String json) {
        try {
            JsonNode node = MAPPER.readTree(json);
            if (!node.isObject()) return null;
            java.util.List<String> ks = new java.util.ArrayList<>();
            node.fieldNames().forEachRemaining(ks::add);
            return String.join(",", ks);
        } catch (Exception e) { return null; }
    }

    // ── Manipulation ─────────────────────────────────────────────────────────

    // Sets key to value; value is parsed as JSON if valid, otherwise stored as string
    public static String set(String json, String key, String value) {
        try {
            ObjectNode obj = (ObjectNode) MAPPER.readTree(json);
            try { obj.set(key, MAPPER.readTree(value)); }
            catch (Exception e2) { obj.put(key, value); }
            return obj.toString();
        } catch (Exception e) { return json; }
    }

    public static String delete(String json, String key) {
        try {
            ObjectNode obj = (ObjectNode) MAPPER.readTree(json);
            obj.remove(key);
            return obj.toString();
        } catch (Exception e) { return json; }
    }

    // Shallow merge — json2 keys overwrite json1 keys
    public static String merge(String json1, String json2) {
        try {
            ObjectNode base = (ObjectNode) MAPPER.readTree(json1);
            ObjectNode overlay = (ObjectNode) MAPPER.readTree(json2);
            overlay.fields().forEachRemaining(e -> base.set(e.getKey(), e.getValue()));
            return base.toString();
        } catch (Exception e) { return json1; }
    }

    public static String pretty(String json) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter()
                         .writeValueAsString(MAPPER.readTree(json));
        } catch (Exception e) { return json; }
    }

    public static String minify(String json) {
        try { return MAPPER.readTree(json).toString(); }
        catch (Exception e) { return json; }
    }

    // ── Array operations ─────────────────────────────────────────────────────

    public static long arrayLength(String json) {
        try {
            JsonNode node = MAPPER.readTree(json);
            return node.isArray() ? node.size() : -1L;
        } catch (Exception e) { return -1L; }
    }

    // Returns element at index as string (raw JSON for objects/arrays)
    public static String arrayGet(String json, int index) {
        try {
            JsonNode node = MAPPER.readTree(json);
            if (!node.isArray()) return null;
            JsonNode elem = node.get(index);
            if (elem == null || elem.isNull()) return null;
            return elem.isTextual() ? elem.asText() : elem.toString();
        } catch (Exception e) { return null; }
    }

    // Returns 1 if any element's string representation equals value
    public static int arrayContainsStr(String json, String value) {
        try {
            JsonNode node = MAPPER.readTree(json);
            if (!node.isArray()) return 0;
            for (JsonNode elem : node) {
                String s = elem.isTextual() ? elem.asText() : elem.toString();
                if (value.equals(s)) return 1;
            }
            return 0;
        } catch (Exception e) { return 0; }
    }

    // Appends value to array; value parsed as JSON if valid, otherwise string
    public static String arrayAppend(String json, String value) {
        try {
            ArrayNode arr = (ArrayNode) MAPPER.readTree(json);
            try { arr.add(MAPPER.readTree(value)); }
            catch (Exception e2) { arr.add(value); }
            return arr.toString();
        } catch (Exception e) { return json; }
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    // Creates {"key": "value"} from two strings
    public static String fromKV(String key, String value) {
        return MAPPER.createObjectNode().put(key, value).toString();
    }

    // Wraps a plain string as a JSON string literal: hello → "hello"
    public static String wrapStr(String value) {
        try { return MAPPER.writeValueAsString(value); }
        catch (Exception e) { return "\"" + value.replace("\"", "\\\"") + "\""; }
    }
}
