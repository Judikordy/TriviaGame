package server;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Minimal JSON read/write utility — no external dependencies.
 * Handles the simple flat structures used in this project.
 */
public class JsonUtil {

    // ---------------------------------------------------------------- writer

    /** Writes a list of maps as a JSON array to a file. */
    public static void writeArray(String filePath, List<Map<String, Object>> records) throws IOException {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < records.size(); i++) {
            sb.append("  ").append(mapToJson(records.get(i)));
            if (i < records.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        Files.write(Paths.get(filePath), sb.toString().getBytes("UTF-8"),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        List<String> keys = new ArrayList<>(map.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            Object v = map.get(k);
            sb.append("\"").append(escape(k)).append("\":");
            sb.append(valueToJson(v));
            if (i < keys.size() - 1) sb.append(",");
        }
        sb.append("}");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static String valueToJson(Object v) {
        if (v == null) return "null";
        if (v instanceof Boolean || v instanceof Integer || v instanceof Long) return v.toString();
        if (v instanceof List) {
            List<?> list = (List<?>) v;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(valueToJson(list.get(i)));
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        }
        if (v instanceof Map) return mapToJson((Map<String, Object>) v);
        return "\"" + escape(v.toString()) + "\"";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    // ---------------------------------------------------------------- reader

    /** Reads a JSON array of objects from a file. Returns list of maps. */
    public static List<Map<String, Object>> readArray(String filePath) throws IOException {
        File f = new File(filePath);
        if (!f.exists()) return new ArrayList<>();
        String content = new String(Files.readAllBytes(f.toPath()), "UTF-8").trim();
        return parseArray(content);
    }

    // ---- recursive descent parser ----

    private static int pos;
    private static String src;

    private static synchronized List<Map<String, Object>> parseArray(String json) {
        src = json;
        pos = 0;
        skipWs();
        expect('[');
        List<Map<String, Object>> list = new ArrayList<>();
        skipWs();
        if (peek() == ']') { pos++; return list; }
        while (true) {
            skipWs();
            list.add(parseObject());
            skipWs();
            if (peek() == ',') { pos++; continue; }
            if (peek() == ']') { pos++; break; }
        }
        return list;
    }

    private static Map<String, Object> parseObject() {
        expect('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipWs();
        if (peek() == '}') { pos++; return map; }
        while (true) {
            skipWs();
            String key = parseString();
            skipWs(); expect(':'); skipWs();
            Object val = parseValue();
            map.put(key, val);
            skipWs();
            if (peek() == ',') { pos++; continue; }
            if (peek() == '}') { pos++; break; }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static Object parseValue() {
        skipWs();
        char c = peek();
        if (c == '"') return parseString();
        if (c == '{') return parseObject();
        if (c == '[') {
            pos++; // consume '['
            List<Object> list = new ArrayList<>();
            skipWs();
            if (peek() == ']') { pos++; return list; }
            while (true) {
                skipWs();
                list.add(parseValue());
                skipWs();
                if (peek() == ',') { pos++; continue; }
                if (peek() == ']') { pos++; break; }
            }
            return list;
        }
        if (c == 't') { pos += 4; return Boolean.TRUE; }
        if (c == 'f') { pos += 5; return Boolean.FALSE; }
        if (c == 'n') { pos += 4; return null; }
        // number
        int start = pos;
        while (pos < src.length() && "0123456789.-+eE".indexOf(src.charAt(pos)) >= 0) pos++;
        String num = src.substring(start, pos);
        try { return Integer.parseInt(num); } catch (NumberFormatException e2) { return num; }
    }

    private static String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') break;
            if (c == '\\') {
                char esc = src.charAt(pos++);
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(esc);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void skipWs() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }

    private static char peek() {
        return pos < src.length() ? src.charAt(pos) : 0;
    }

    private static void expect(char c) {
        if (pos >= src.length() || src.charAt(pos) != c)
            throw new RuntimeException("Expected '" + c + "' at pos " + pos + " but got '" + peek() + "'");
        pos++;
    }
}
