package com.s23010162.safewalk;

import java.io.Serializable;

/**
 * User Profile data model class
 * Implements Serializable for easy data passing between activities
 */
public class UserProfile implements Serializable {

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
        return emergencyPin != null &&
                emergencyPin.length() == 4 &&
                emergencyPin.matches("\\d{4}");
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
