package com.s23010162.safewalk.utils;

/**
 * Constants used throughout the SafeWalk app
 * Centralizes magic numbers and string constants
 */
public class Constants {
    
    // Permission request codes
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final int SMS_PERMISSION_REQUEST_CODE = 101;
    public static final int EMERGENCY_PERMISSIONS_REQUEST_CODE = 123;
    public static final int CALL_PHONE_PERMISSION_REQUEST_CODE = 456;
    public static final int REGISTRATION_PERMISSION_REQUEST_CODE = 1001;
    
    // Timer intervals
    public static final long TIMER_UPDATE_INTERVAL = 1000; // 1 second
    public static final long LOCATION_UPDATE_INTERVAL = 30000; // 30 seconds
    public static final long LOCATION_FASTEST_INTERVAL = 15000; // 15 seconds
    public static final long LOCATION_REQUEST_INTERVAL = 10000; // 10 seconds
    public static final long LOCATION_REQUEST_FASTEST_INTERVAL = 5000; // 5 seconds
    
    // Animation durations
    public static final long RECORDING_DOT_ANIMATION_DURATION = 500; // 500ms
    public static final long RECORDING_DOT_ANIMATION_OFFSET = 20; // 20ms
    
    // UI delays
    public static final long EMERGENCY_ACTIONS_DELAY = 1500; // 1.5 seconds
    
    // PIN validation
    public static final int PIN_LENGTH = 4;
    public static final String PIN_REGEX = "\\d{4}";
    
    // Phone number validation
    public static final int MIN_PHONE_LENGTH = 10;
    
    // Name validation
    public static final int MIN_NAME_LENGTH = 2;
    
    // Map settings
    public static final float DEFAULT_MAP_ZOOM = 16.0f;
    
    // Emergency services
    public static final String EMERGENCY_SERVICES_NUMBER = "tel:"; // Placeholder for 119 or other services
    public static final String TEST_PHONE_NUMBER = "tel:+1234567890";
    
    // File extensions
    public static final String AUDIO_FILE_EXTENSION = ".mp3";
    
    // Database operations
    public static final int SINGLE_LOCATION_UPDATE = 1;
    
    // UI states
    public static final float DISABLED_ALPHA = 0.5f;
    public static final float ENABLED_ALPHA = 1.0f;
    
    // Error messages
    public static final String ERROR_LOCATION_PERMISSION = "Location permission not granted";
    public static final String ERROR_SMS_PERMISSION = "SMS permission denied";
    public static final String ERROR_CALL_PERMISSION = "Call permission is required to make emergency calls";
    public static final String ERROR_NO_CONTACTS = "No emergency contacts found";
    public static final String ERROR_LOCATION_UNAVAILABLE = "Location not available";
    public static final String ERROR_ADDRESS_UNAVAILABLE = "Could not get address";
    
    // Success messages
    public static final String SUCCESS_SOS_SENT = "Emergency SOS sent to %d contact(s)";
    public static final String SUCCESS_ACCOUNT_CREATED = "Account created successfully!";
    public static final String SUCCESS_ALERT_CANCELED = "Alert Canceled Successfully";
    public static final String SUCCESS_SOS_CANCELED = "SOS Canceled";
    
    // Validation messages
    public static final String VALIDATION_ENTER_PIN = "Please enter your PIN";
    public static final String VALIDATION_PIN_4_DIGITS = "PIN must be 4 digits";
    public static final String VALIDATION_INCORRECT_PIN = "Incorrect PIN";
    public static final String VALIDATION_ENTER_NAME = "Please enter a valid full name";
    public static final String VALIDATION_ENTER_EMAIL = "Please enter a valid email address";
    public static final String VALIDATION_ENTER_PHONE = "Please enter a valid phone number";
    public static final String VALIDATION_PINS_MATCH = "PINs do not match";
    public static final String VALIDATION_FIX_ERRORS = "Please fix all errors before proceeding";
}
