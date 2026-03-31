#  SafeWalk - Personal Safety Mobile Application

**Empowering travelers, campers, and individuals in potentially unsafe environments with real-time safety monitoring and emergency response capabilities.**

SafeWalk is a comprehensive personal safety application for Android that provides instant emergency alerts, real-time location tracking, and evidence recording—all designed to keep you safe when it matters most.

---

## ✨ Key Features

- 🚨 **SOS Emergency Triggers** - Shake detection and manual triggers to activate emergency mode instantly
- 📍 **Real-Time Location Tracking** - Continuous GPS tracking with map visualization via Google Maps
- 📱 **Automated Emergency Alerts** - Instant SMS and phone calls to your emergency contacts
- 🎥 **Background Recording** - Automatic video and audio recording for incident evidence and documentation
- 👥 **Emergency Contact Management** - Easy setup and management of trusted emergency contacts
- 🚶 **Walk Mode with Live Tracking** - Activate dedicated walk mode for real-time location sharing
- 📊 **Alert History & Media Library** - Complete history of all alerts and stored recordings for reference
- 🔐 **Secure Data Storage** - All sensitive information encrypted using Android Keystore

---

## 🛠️ Tech Stack

### Language
- **Java** - Core application development 

### IDE & Build Tools
- **Android Studio** - Official Android development environment
- **Gradle** (Kotlin DSL) - Build automation and dependency management
- **Android API Level 24-34** - Target SDK 34 (Android 14), Minimum SDK 24 (Android 7.0)

### Database
- **Room Database (v2.6.1)** - Local SQLite database for offline data persistence
  - Emergency contact storage
  - Alert history
  - Location data caching

### APIs & Libraries
- **Google Maps SDK (v18.2.0)** - Real-time location visualization and mapping
- **Google Play Services Location (v21.2.0)** - Precise GPS and location services
- **CameraX (v1.2.3)** - Advanced camera and video recording capabilities
- **AndroidX Libraries**
  - AppCompat (v1.6.1)
  - Navigation (v2.7.7)
  - Material Design (v1.11.0)
  - Constraint Layout (v2.1.4)

---

## 📦 Installation Guide

### Prerequisites
- Android Studio (latest version recommended)
- Java Development Kit (JDK) 11 or higher
- Google Maps API Key (free tier available)
- Android device or emulator (API 24+)

### Step 1: Clone the Repository
```bash
git clone https://github.com/Mrghost077/SafeWalk.git
cd SafeWalk
```

### Step 2: Open in Android Studio
1. Launch Android Studio
2. Select **File > Open**
3. Navigate to the SafeWalk folder and click **OK**
4. Android Studio will automatically sync Gradle files

### Step 3: Configure Google Maps API Key
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or select an existing one)
3. Enable the **Maps SDK for Android**
4. Create an **API Key** credential
5. Open `local.properties` in your project root directory
6. Add your API key:
   ```properties
   MAPS_API_KEY=your_actual_api_key_here
   ```
7. **Important:** The `local.properties` file is already in `.gitignore` for security—never commit your actual API key to version control

### Step 4: Build and Run
1. Sync the project with Gradle files (**File > Sync Now**)
2. Select your target device (physical device or emulator)
3. Click **Run** (green play button) or press `Shift + F10`
4. The app will build and launch on your device

---


## 🏗️ Architecture

SafeWalk follows **Object-Oriented Programming (OOP)** principles with a clean separation of concerns:

- **Activities** - UI layer handling user interactions
- **Services** - Background services for location tracking, shake detection, and recording
- **Database Layer** - Room for data persistence and offline functionality
- **API Integration** - Google Maps and Play Services for location and mapping

---

## 🔒 Security & Permissions

### Data Security
- All sensitive data (contact information, location history, recordings) is encrypted using **Android Keystore**
- API keys are securely stored in `local.properties` and not exposed in source control
- Runtime permissions are requested only when needed

### Required Permissions
- **Location Access** (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`) - For safety tracking features
- **Camera** - For video evidence recording
- **Microphone** (`RECORD_AUDIO`) - For audio recording during incidents
- **SMS & Phone Call** (`SEND_SMS`, `CALL_PHONE`) - For emergency alerts
- **Storage** (`READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`) - For managing recordings
- **Internet** - For maps and location services
- **Foreground Service** - For background tracking and recording
- **Vibration** - For SOS feedback

---

## 📋 Target Audience

- **Travelers** - Solo travelers exploring unfamiliar locations
- **Campers & Outdoor Enthusiasts** - Safety during outdoor adventures
- **Night Workers** - Drivers, security personnel, delivery workers
- **Anyone Requiring Personal Safety** - Individuals in potentially unsafe environments

---

## 🚀 Getting Started

1. **Setup Emergency Contacts** - Add trusted contacts who will receive alerts
2. **Enable Location Services** - Grant necessary permissions for tracking
3. **Test SOS Feature** - Familiarize yourself with the shake detection or manual alert button
4. **Review Recording Settings** - Configure video/audio recording preferences
5. **Start Your Walk** - Activate walk mode and share your real-time location

---

## 📝 Notes

- The app works best with an active internet connection for real-time features
- Background recording requires sufficient device storage
- Location tracking is optimized for battery efficiency
- Ensure emergency contacts have SMS capability enabled

---

## 👨‍💻 Developer

Built during 2nd year at **Open University of Sri Lanka (OUSL)** as a comprehensive personal safety solution.

---


---

**Stay Safe. Stay Connected. SafeWalk.**
