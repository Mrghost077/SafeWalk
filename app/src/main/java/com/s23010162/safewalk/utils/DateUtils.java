package com.s23010162.safewalk.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date formatting operations
 * Centralizes all date formatting patterns used throughout the app
 */
public class DateUtils {
    
    // Date format patterns
    public static final String PATTERN_TIME_ONLY = "hh:mm a";
    public static final String PATTERN_DATE_TIME = "MMM dd, yyyy 'at' hh:mm a";
    public static final String PATTERN_DATE_ONLY = "MMM dd, yyyy";
    public static final String PATTERN_DATE_TIME_24H = "MMM dd, yyyy - HH:mm";
    public static final String PATTERN_FILE_NAME = "yyyy-MM-dd-HH-mm-ss";
    public static final String PATTERN_TIMESTAMP = "yyyyMMdd_HHmmss";
    public static final String PATTERN_WALK_DATE = "EEE, dd MMM yyyy";
    
    // Timer format pattern
    public static final String PATTERN_TIMER = "%02d:%02d";
    
    /**
     * Format time in HH:MM format
     */
    public static String formatTime(long millis) {
        long minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format(Locale.getDefault(), PATTERN_TIMER, minutes, seconds);
    }
    
    /**
     * Format date with time only (hh:mm a)
     */
    public static String formatTimeOnly(Date date) {
        return new SimpleDateFormat(PATTERN_TIME_ONLY, Locale.getDefault()).format(date);
    }
    
    /**
     * Format date with time only (hh:mm a) from timestamp
     */
    public static String formatTimeOnly(long timestamp) {
        return formatTimeOnly(new Date(timestamp));
    }
    
    /**
     * Format date and time for alert history
     */
    public static String formatDateTime(Date date) {
        return new SimpleDateFormat(PATTERN_DATE_TIME, Locale.getDefault()).format(date);
    }
    
    /**
     * Format date only for recordings list
     */
    public static String formatDateOnly(Date date) {
        return new SimpleDateFormat(PATTERN_DATE_ONLY, Locale.getDefault()).format(date);
    }
    
    /**
     * Format date and time with 24-hour format
     */
    public static String formatDateTime24H(Date date) {
        return new SimpleDateFormat(PATTERN_DATE_TIME_24H, Locale.getDefault()).format(date);
    }
    
    /**
     * Format date for file names
     */
    public static String formatForFileName(Date date) {
        return new SimpleDateFormat(PATTERN_FILE_NAME, Locale.US).format(date);
    }
    
    /**
     * Format timestamp for recording files
     */
    public static String formatTimestamp(Date date) {
        return new SimpleDateFormat(PATTERN_TIMESTAMP, Locale.getDefault()).format(date);
    }
    
    /**
     * Format date for walk history
     */
    public static String formatWalkDate(Date date) {
        return new SimpleDateFormat(PATTERN_WALK_DATE, Locale.getDefault()).format(date);
    }
}
