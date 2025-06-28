package com.s23010162.safewalk;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * User Profile data model class
 * Implements Serializable for easy data passing between activities
 */
@Entity(tableName = "user_profile")
public class UserProfile implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    private String fullName;
    private String emailAddress;
    private String password;
    private String phoneNumber;
    private String emergencyPin;
    private boolean locationAccessEnabled;
    private boolean cameraAccessEnabled;
    private long createdTimestamp;
    private boolean isProfileComplete;

    // Default constructor
    public UserProfile() {
        this.createdTimestamp = System.currentTimeMillis();
        this.isProfileComplete = false;
    }

    // Constructor with parameters
    public UserProfile(String fullName, String emailAddress, String phoneNumber,
                       String emergencyPin, boolean locationAccessEnabled,
                       boolean cameraAccessEnabled) {
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.emergencyPin = emergencyPin;
        this.locationAccessEnabled = locationAccessEnabled;
        this.cameraAccessEnabled = cameraAccessEnabled;
        this.createdTimestamp = System.currentTimeMillis();
        this.isProfileComplete = true;
    }

    // Constructor for EditProfileFragment
    public UserProfile(String name, String email, String password) {
        this.fullName = name;
        this.emailAddress = email;
        this.password = password;
    }

    // Getters
    public String getFullName() {
        return fullName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmergencyPin() {
        return emergencyPin;
    }

    public boolean isLocationAccessEnabled() {
        return locationAccessEnabled;
    }

    public boolean isCameraAccessEnabled() {
        return cameraAccessEnabled;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public boolean isProfileComplete() {
        return isProfileComplete;
    }

    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmergencyPin(String emergencyPin) {
        this.emergencyPin = emergencyPin;
    }

    public void setLocationAccessEnabled(boolean locationAccessEnabled) {
        this.locationAccessEnabled = locationAccessEnabled;
    }

    public void setCameraAccessEnabled(boolean cameraAccessEnabled) {
        this.cameraAccessEnabled = cameraAccessEnabled;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public void setProfileComplete(boolean profileComplete) {
        this.isProfileComplete = profileComplete;
    }

    // Validation methods
    public boolean isValidEmail() {
        return emailAddress != null &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches();
    }

    public boolean isValidPhoneNumber() {
        return phoneNumber != null &&
                phoneNumber.replaceAll("[^\\d]", "").length() >= 10;
    }

    public boolean isValidPin() {
        if (emergencyPin == null || emergencyPin.length() != 4 || !emergencyPin.matches("\\d{4}")) {
            return false;
        }
        
        // Check for common weak patterns
        String pin = emergencyPin;
        
        // Check for sequential numbers (1234, 4321, etc.)
        if (isSequential(pin)) {
            return false;
        }
        
        // Check for repeated numbers (1111, 2222, etc.)
        if (isRepeated(pin)) {
            return false;
        }
        
        // Check for common patterns (0000, 9999, etc.)
        if (pin.equals("0000") || pin.equals("9999") || pin.equals("1234") || 
            pin.equals("4321") || pin.equals("1111") || pin.equals("2222") ||
            pin.equals("3333") || pin.equals("4444") || pin.equals("5555") ||
            pin.equals("6666") || pin.equals("7777") || pin.equals("8888")) {
            return false;
        }
        
        return true;
    }
    
    private boolean isSequential(String pin) {
        int[] digits = new int[4];
        for (int i = 0; i < 4; i++) {
            digits[i] = Character.getNumericValue(pin.charAt(i));
        }
        
        // Check ascending sequence
        boolean ascending = true;
        for (int i = 1; i < 4; i++) {
            if (digits[i] != digits[i-1] + 1) {
                ascending = false;
                break;
            }
        }
        
        // Check descending sequence
        boolean descending = true;
        for (int i = 1; i < 4; i++) {
            if (digits[i] != digits[i-1] - 1) {
                descending = false;
                break;
            }
        }
        
        return ascending || descending;
    }
    
    private boolean isRepeated(String pin) {
        char first = pin.charAt(0);
        for (int i = 1; i < 4; i++) {
            if (pin.charAt(i) != first) {
                return false;
            }
        }
        return true;
    }

    public boolean isValidName() {
        return fullName != null &&
                !fullName.trim().isEmpty() &&
                fullName.trim().length() >= 2;
    }

    // Check if all required fields are valid
    public boolean isAllFieldsValid() {
        return isValidName() && isValidEmail() && isValidPhoneNumber() && isValidPin();
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "fullName='" + fullName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", locationAccessEnabled=" + locationAccessEnabled +
                ", cameraAccessEnabled=" + cameraAccessEnabled +
                ", createdTimestamp=" + createdTimestamp +
                ", isProfileComplete=" + isProfileComplete +
                '}';
    }
}
