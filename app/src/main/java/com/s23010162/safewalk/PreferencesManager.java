package com.s23010162.safewalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import com.s23010162.safewalk.UserProfile;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.KeyStore;

/**
 * Manages user preferences and profile data with encryption for sensitive information
 */
public class PreferencesManager {

    private static final String PREF_NAME = "UserProfilePrefs";
    private static final String KEY_ALIAS = "UserProfileKey";

    // Preference keys
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email_address";
    private static final String KEY_PHONE = "phone_number";
    private static final String KEY_EMERGENCY_PIN = "emergency_pin";
    private static final String KEY_LOCATION_ACCESS = "location_access";
    private static final String KEY_CAMERA_ACCESS = "camera_access";
    private static final String KEY_CREATED_TIMESTAMP = "created_timestamp";
    private static final String KEY_IS_PROFILE_COMPLETE = "is_profile_complete";
    private static final String KEY_IS_FIRST_LAUNCH = "is_first_launch";

    private SharedPreferences sharedPreferences;
    private Context context;

    public PreferencesManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save user profile data
     */
    public boolean saveUserProfile(UserProfile userProfile) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(KEY_FULL_NAME, userProfile.getFullName());
            editor.putString(KEY_EMAIL, userProfile.getEmailAddress());
            editor.putString(KEY_PHONE, userProfile.getPhoneNumber());

            // Encrypt sensitive data like PIN
            String encryptedPin = encryptData(userProfile.getEmergencyPin());
            editor.putString(KEY_EMERGENCY_PIN, encryptedPin);

            editor.putBoolean(KEY_LOCATION_ACCESS, userProfile.isLocationAccessEnabled());
            editor.putBoolean(KEY_CAMERA_ACCESS, userProfile.isCameraAccessEnabled());
            editor.putLong(KEY_CREATED_TIMESTAMP, userProfile.getCreatedTimestamp());
            editor.putBoolean(KEY_IS_PROFILE_COMPLETE, userProfile.isProfileComplete());
            editor.putBoolean(KEY_IS_FIRST_LAUNCH, false);

            return editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieve user profile data
     */
    public UserProfile getUserProfile() {
        try {
            if (!isProfileComplete()) {
                return null;
            }

            UserProfile profile = new UserProfile();
            profile.setFullName(sharedPreferences.getString(KEY_FULL_NAME, ""));
            profile.setEmailAddress(sharedPreferences.getString(KEY_EMAIL, ""));
            profile.setPhoneNumber(sharedPreferences.getString(KEY_PHONE, ""));

            // Decrypt sensitive data
            String encryptedPin = sharedPreferences.getString(KEY_EMERGENCY_PIN, "");
            String decryptedPin = decryptData(encryptedPin);
            profile.setEmergencyPin(decryptedPin);

            profile.setLocationAccessEnabled(sharedPreferences.getBoolean(KEY_LOCATION_ACCESS, false));
            profile.setCameraAccessEnabled(sharedPreferences.getBoolean(KEY_CAMERA_ACCESS, false));
            profile.setCreatedTimestamp(sharedPreferences.getLong(KEY_CREATED_TIMESTAMP, 0));
            profile.setProfileComplete(sharedPreferences.getBoolean(KEY_IS_PROFILE_COMPLETE, false));

            return profile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if this is the first time the app is launched
     */
    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean isFirstLaunch) {
        sharedPreferences.edit().putBoolean(KEY_IS_FIRST_LAUNCH, isFirstLaunch).apply();
    }


    /**
     * Check if user profile is complete
     */
    public boolean isProfileComplete() {
        return sharedPreferences.getBoolean(KEY_IS_PROFILE_COMPLETE, false);
    }

    /**
     * Clear all user data (for logout or reset)
     */
    public boolean clearUserData() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.putBoolean(KEY_IS_FIRST_LAUNCH, false); // Keep this to avoid onboarding again
            return editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verify emergency PIN
     */
    public boolean verifyEmergencyPin(String inputPin) {
        try {
            String encryptedPin = sharedPreferences.getString(KEY_EMERGENCY_PIN, "");
            String storedPin = decryptData(encryptedPin);
            return storedPin.equals(inputPin);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Simple encryption for demonstration (use Android Keystore in production)
     * Note: This is a simplified version. For production apps, use Android Keystore properly
     */
    private String encryptData(String data) {
        try {
            // For simplicity, using Base64 encoding
            // In production, use proper encryption with Android Keystore
            byte[] encrypted = Base64.encode(data.getBytes(), Base64.DEFAULT);
            return new String(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return data; // Return original if encryption fails
        }
    }

    /**
     * Simple decryption for demonstration
     */
    private String decryptData(String encryptedData) {
        try {
            // For simplicity, using Base64 decoding
            // In production, use proper decryption with Android Keystore
            byte[] decrypted = Base64.decode(encryptedData.getBytes(), Base64.DEFAULT);
            return new String(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return ""; // Return empty if decryption fails
        }
    }
}