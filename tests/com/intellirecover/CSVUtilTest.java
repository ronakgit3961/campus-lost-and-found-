package com.intellirecover;

import com.intellirecover.storage.CSVUtil;
import java.util.Arrays;
import java.util.List;

public class CSVUtilTest {
    public static void testCSVUtil() {
        System.out.println("Running CSVUtil tests...");

        // Test simple fields
        List<String> simple = CSVUtil.parseLine("a,b,c");
        assert simple.size() == 3 : "Simple size mismatch";
        assert "a".equals(simple.get(0));
        assert "b".equals(simple.get(1));
        assert "c".equals(simple.get(2));

        // Test commas inside quotes
        List<String> withCommas = CSVUtil.parseLine("\"Central Library, 2nd floor\",Books,\"Item, with comma\"");
        assert withCommas.size() == 3 : "Commas in quotes size mismatch";
        assert "Central Library, 2nd floor".equals(withCommas.get(0));
        assert "Books".equals(withCommas.get(1));
        assert "Item, with comma".equals(withCommas.get(2));

        // Test escaped quotes
        List<String> withQuotes = CSVUtil.parseLine("\"Item with \"\"quoted\"\" name\",FND-1001");
        assert withQuotes.size() == 2;
        assert "Item with \"quoted\" name".equals(withQuotes.get(0));

        // Test roundtrip
        List<String> original = Arrays.asList("LST-1001", "Black Bottle, 750ml", "Contains \"water\" label", "LIBRARY");
        String formatted = CSVUtil.formatLine(original);
        List<String> parsedBack = CSVUtil.parseLine(formatted);
        assert parsedBack.equals(original) : "Roundtrip failed: " + parsedBack + " vs " + original;

        System.out.println("-> CSVUtil tests PASSED!");
    }
}
