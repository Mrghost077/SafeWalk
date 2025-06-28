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
import javax.crypto.spec.GCMParameterSpec;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * Manages user preferences and profile data with encryption for sensitive information
 */
public class PreferencesManager {

    private static final String PREF_NAME = "UserProfilePrefs";
    private static final String KEY_ALIAS = "SafeWalkUserProfileKey";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

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

    // Settings Keys
    private static final String KEY_SHAKE_SENSITIVITY_ENABLED = "shake_sensitivity_enabled";
    private static final String KEY_AUTO_RECORDING_ENABLED = "auto_recording_enabled";
    private static final String KEY_LOCATION_TRACKING_ENABLED = "location_tracking_enabled";
    private static final String KEY_OFFLINE_MODE_ENABLED = "offline_mode_enabled";

    private SharedPreferences sharedPreferences;
    private Context context;
    private KeyStore keyStore;

    public PreferencesManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        initializeKeyStore();
    }

    private void initializeKeyStore() {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            
            // Create key if it doesn't exist
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                createKey();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        
        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .setRandomizedEncryptionRequired(true)
                .build();
        
        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
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
     * Set user profile data (alias for saveUserProfile)
     */
    public void setUserProfile(UserProfile userProfile) {
        saveUserProfile(userProfile);
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

    // --- Getters for new settings ---
    public boolean isShakeDetectionEnabled() { return sharedPreferences.getBoolean(KEY_SHAKE_SENSITIVITY_ENABLED, true); }
    public boolean isAutoRecordingEnabled() { return sharedPreferences.getBoolean(KEY_AUTO_RECORDING_ENABLED, true); }
    public boolean isLocationTrackingEnabled() { return sharedPreferences.getBoolean(KEY_LOCATION_TRACKING_ENABLED, true); }
    public boolean isOfflineModeEnabled() { return sharedPreferences.getBoolean(KEY_OFFLINE_MODE_ENABLED, false); }

    // --- Setters for new settings ---
    public void setShakeDetectionEnabled(boolean enabled) { sharedPreferences.edit().putBoolean(KEY_SHAKE_SENSITIVITY_ENABLED, enabled).apply(); }
    public void setAutoRecordingEnabled(boolean enabled) { sharedPreferences.edit().putBoolean(KEY_AUTO_RECORDING_ENABLED, enabled).apply(); }
    public void setLocationTrackingEnabled(boolean enabled) { sharedPreferences.edit().putBoolean(KEY_LOCATION_TRACKING_ENABLED, enabled).apply(); }
    public void setOfflineModeEnabled(boolean enabled) { sharedPreferences.edit().putBoolean(KEY_OFFLINE_MODE_ENABLED, enabled).apply(); }

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
     * Proper encryption using Android Keystore
     */
    private String encryptData(String data) {
        try {
            if (data == null || data.isEmpty()) {
                return "";
            }

            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // Generate random IV
            SecureRandom secureRandom = new SecureRandom();
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
            
            byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));
            
            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            
            return Base64.encodeToString(combined, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return data; // Return original if encryption fails
        }
    }

    /**
     * Proper decryption using Android Keystore
     */
    private String decryptData(String encryptedData) {
        try {
            if (encryptedData == null || encryptedData.isEmpty()) {
                return "";
            }

            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            byte[] combined = Base64.decode(encryptedData, Base64.DEFAULT);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
            
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return encryptedData; // Return original if decryption fails
        }
    }
}