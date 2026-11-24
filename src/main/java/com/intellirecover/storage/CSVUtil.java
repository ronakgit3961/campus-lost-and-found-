package com.intellirecover.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Robust RFC-4180 compliant CSV utility.
 * Correctly parses and formats records containing commas, quotation marks, and line breaks
 * without naive String.split(",").
 */
public final class CSVUtil {

    private CSVUtil() {
        // Prevent instantiation
    }

    /**
     * Parses a single CSV line into tokens, respecting double-quote encapsulation.
     */
    public static List<String> parseLine(String line) {
        List<String> result = new ArrayList<>();
        if (line == null) {
            return result;
        }

        StringBuilder curVal = new StringBuilder();
        boolean inQuotes = false;
        int len = line.length();

        for (int i = 0; i < len; i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    // Check for escaped double quote ("")
                    if (i + 1 < len && line.charAt(i + 1) == '"') {
                        curVal.append('"');
                        i++; // Skip the second quote
                    } else {
                        inQuotes = false;
                    }
                } else {
                    curVal.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    result.add(curVal.toString());
                    curVal.setLength(0);
                } else {
                    curVal.append(c);
                }
            }
        }
        result.add(curVal.toString());
        return result;
    }

    /**
     * Reads a full record from a BufferedReader, seamlessly supporting fields
     * that span multiple physical lines inside quoted blocks.
     */
    public static List<String> readRecord(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }

        StringBuilder fullRecord = new StringBuilder(line);
        while (countUnescapedQuotes(fullRecord.toString()) % 2 != 0) {
            String nextLine = reader.readLine();
            if (nextLine == null) {
                break; // Premature EOF
            }
            fullRecord.append("\n").append(nextLine);
        }

        return parseLine(fullRecord.toString());
    }

    private static int countUnescapedQuotes(String s) {
        int count = 0;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) == '"') {
                count++;
            }
        }
        return count;
    }

    /**
     * Formats a list of string fields into a compliant CSV line.
     * Escapes fields containing commas, double-quotes, or newlines.
     */
    public static String formatLine(List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapeField(fields.get(i)));
        }
        return sb.toString();
    }

    public static String escapeField(String field) {
        if (field == null) {
            return "";
        }
        boolean mustQuote = field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r");
        if (!mustQuote) {
            return field;
        }
        String escaped = field.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
