package com.intellirecover;

import com.intellirecover.ui.ConsoleUI;

/**
 * Main application entry point for Campus Lost & Found – IntelliRecover.
 */
public class Main {
    public static void main(String[] args) {
        try {
            ConsoleUI ui = new ConsoleUI();
            ui.start();
        } catch (Exception e) {
            System.err.println("\n[FATAL] An unexpected application error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
