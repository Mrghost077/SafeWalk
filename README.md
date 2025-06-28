# SafeWalk - Personal Safety App

A comprehensive personal safety application for Android that provides emergency alerts, location sharing, and recording capabilities.

## Setup Instructions

### 1. Google Maps API Key Setup

To use the maps functionality, you need to add your Google Maps API key:

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Maps SDK for Android
4. Create credentials (API Key)
5. Open `local.properties` in the project root
6. Add your API key:
   ```
   MAPS_API_KEY=your_actual_api_key_here
   ```

**Important**: Never commit your actual API key to version control. The `local.properties` file is already in `.gitignore`.

### 2. Build and Run

1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Build and run the application

## Features

- Emergency shake detection
- Location tracking and sharing
- Video recording for evidence
- Emergency contact management
- Walk mode with real-time tracking
- Alert history and recordings

## Security Notes

- All sensitive data is encrypted using Android Keystore
- API keys are stored securely in local.properties
- Permissions are requested at runtime as needed

## Permissions Required

- Location access (for safety features)
- Camera and microphone (for recording)
- SMS (for emergency alerts)
- Storage (for saving recordings) 