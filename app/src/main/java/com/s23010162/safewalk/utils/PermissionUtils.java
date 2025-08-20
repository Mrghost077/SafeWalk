package com.s23010162.safewalk.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

/**
 * Utility class for permission operations
 * Centralizes permission checking logic used throughout the app
 */
public class PermissionUtils {
    
    /**
     * Check if location permission is granted
     */
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if SMS permission is granted
     */
    public static boolean hasSmsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if camera permission is granted
     */
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if audio recording permission is granted
     */
    public static boolean hasAudioPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if call phone permission is granted
     */
    public static boolean hasCallPhonePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if all emergency permissions are granted
     */
    public static boolean hasAllEmergencyPermissions(Context context) {
        return hasSmsPermission(context) && 
               hasCameraPermission(context) && 
               hasAudioPermission(context);
    }
    
    /**
     * Get location permission request array
     */
    public static String[] getLocationPermissions() {
        return new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }
    
    /**
     * Get emergency permissions request array
     */
    public static String[] getEmergencyPermissions() {
        return new String[]{
            Manifest.permission.SEND_SMS,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        };
    }
    
    /**
     * Get camera and audio permissions request array
     */
    public static String[] getCameraAudioPermissions() {
        return new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }
}
