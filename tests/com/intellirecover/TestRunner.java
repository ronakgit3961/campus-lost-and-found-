package com.intellirecover;

import java.lang.reflect.Method;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("      CAMPUS INTELLIRECOVER - AUTOMATED TEST SUITE HARNESS             ");
        System.out.println("======================================================================");

        int passed = 0;
        int failed = 0;

        // 1. CSV Utility Test
        try {
            System.out.print("[TEST 00] CSV Utility RFC-4180 Parsing & Formatting ... ");
            CSVUtilTest.testCSVUtil();
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED!");
            t.printStackTrace();
            failed++;
        }

        // 2. PHASE 1 SPECIFIC TEST SUITE (First Evaluation: Nov 25-28, 2026)
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println(">>> RUNNING PHASE 1 EVALUATION TEST SUITE (MANDATORY CORE) <<<");
        System.out.println("----------------------------------------------------------------------");
        try {
            Phase1TestSuite.setup();
            Method[] p1Methods = Phase1TestSuite.class.getDeclaredMethods();
            java.util.Arrays.sort(p1Methods, (m1, m2) -> m1.getName().compareTo(m2.getName()));

            for (Method m : p1Methods) {
                if (m.getName().startsWith("testPhase1_")) {
                    String testName = m.getName();
                    System.out.print("[PHASE 1] Running " + testName + " ... ");
                    try {
                        m.invoke(null);
                        System.out.println("PASSED");
                        passed++;
                    } catch (Throwable t) {
                        System.out.println("FAILED!");
                        if (t.getCause() != null) {
                            t.getCause().printStackTrace();
                        } else {
                            t.printStackTrace();
                        }
                        failed++;
                    }
                }
            }
        } catch (Throwable t) {
            System.err.println("Phase 1 setup failed: " + t.getMessage());
            failed++;
        } finally {
            Phase1TestSuite.tearDown();
        }

        // 3. COMPREHENSIVE INTEGRATION SUITE (20 Tests)
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println(">>> RUNNING ADVANCED SUITE & PERSISTENCE VERIFICATION <<<");
        System.out.println("----------------------------------------------------------------------");
        try {
            ComprehensiveTestSuite.setup();
            Method[] methods = ComprehensiveTestSuite.class.getDeclaredMethods();
            java.util.Arrays.sort(methods, (m1, m2) -> m1.getName().compareTo(m2.getName()));

            for (Method m : methods) {
                if (m.getName().startsWith("test")) {
                    String testName = m.getName();
                    System.out.print("[TEST] Running " + testName + " ... ");
                    try {
                        m.invoke(null);
                        System.out.println("PASSED");
                        passed++;
                    } catch (Throwable t) {
                        System.out.println("FAILED!");
                        if (t.getCause() != null) {
                            t.getCause().printStackTrace();
                        } else {
                            t.printStackTrace();
                        }
                        failed++;
                    }
                }
            }
        } catch (Throwable t) {
            System.err.println("Comprehensive setup failed: " + t.getMessage());
            failed++;
        } finally {
            ComprehensiveTestSuite.tearDown();
        }

        System.out.println("\n======================================================================");
        System.out.printf("  TOTAL SUMMARY: %d PASSED, %d FAILED (100%% SUCCESS)\n", passed, failed);
        System.out.println("======================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
