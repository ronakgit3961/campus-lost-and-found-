package com.intellirecover.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Utility for verifying user inputs, preventing crashes and dirty data.
 */
public final class InputValidator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{3,24}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+\\- ]{7,16}$");

    private InputValidator() {
        // Prevent instantiation
    }

    public static boolean isNonEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidPastOrPresentDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isAfter(LocalDate.now());
    }

    public static int parsePositiveInt(String text, int defaultValue) {
        if (text == null) return defaultValue;
        try {
            int val = Integer.parseInt(text.trim());
            return val > 0 ? val : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
