# Behavior-Based Authentication System — SURAKSHAK

## Overview

**Surakshak** is a secure mobile authentication system that extends beyond conventional security approaches by integrating behavioral biometrics, panic gestures, and simulated (fake) environments. These elements work together to detect unauthorized access and silently safeguard sensitive user data.

The system offers continuous, adaptive, and covert protection by identifying the legitimate user's unique interaction patterns. It calculates a real-time **risk score** using a machine learning model and reauthenticates users if anomalies are detected.

---

## Application Flow

### 1. Signup Screen

* User enters name and mobile number.
* OTP is sent via backend.
* Upon OTP verification:
  * A unique `userID` (UUID) is generated.
  * Stored in `SharedPreferences`.
  * Passed to the next activity.

### 2. Set Passcode

* User creates a six-digit passcode.
* Confirmed with second OTP.
* Stored securely in MongoDB.
* Triggers transition to behavior training.

### 3. Behavioral Training Session (New)

* Launches `TrainingSessionActivity`.
* Displays 4 training sentences for the user to type.
* Collects behavioral traits:
  * Typing speed
  * Dwell time
  * Flight time
  * Tap pressure
  * Inter-key delay
  * Touch size
  * Screen hold time
  * Accelerometer and gyroscope patterns
* Sends data via `/upload-training-batch`.
* Triggers training with `/train-hybrid-model`.
* Calculates average behavior vector.
* Stores baseline in MongoDB.

### 4. Login Screen

* Login via:
  * Passcode
  * Fingerprint (BiometricPrompt)
  * OTP (Forgot Passcode)
* After login:
  * Retrieves mobile number and `userID`.
  * Redirects to home screen.

### 5. Home Screen

* Displays user details, logout, and feature grid.
* Panic gesture detection triggers fake home.
* Behavior monitoring starts automatically.

#### Real-Time Monitoring (New)

* Integrated via `BehaviorMonitor.java`.
* Monitors:
  * Typing patterns
  * Touch metrics
  * Motion sensors
  * Screen hold time
* Every 20 seconds:
  * Constructs a behavior vector.
  * Sends to `/evaluate-risk`.
  * Logs or reacts based on returned risk score.

---

## New Files & Features Added

### ✅ `BehaviorMonitor.java`

* Standalone class for collecting sensor and touch-based metrics continuously.
* Runs inside `Homescreen.java` and other key screens.
* Sends vector to backend at 20s intervals.

### ✅ `RiskAnalyzer.java`

* Handles sending behavioral vectors and logging risk scores inside Android app.

### ✅ Updated `Homescreen.java`

* Integrated `BehaviorMonitor`.
* Fetches `userID` from `SharedPreferences`.
* Handles panic gestures and logout.
* Sends behavior vectors to backend API.

### ✅ Updated `TrainingSessionActivity.java`

* Collects behavioral training data.
* Sends to `/upload-training-batch`.
* Triggers `/train-hybrid-model`.

### ✅ Backend — Flask `app.py` Updates

* **New Route:** `/evaluate-risk`:
  * Accepts incoming vector.
  * Fetches stored baseline.
  * Calculates risk score.
  * Returns normalized risk between `0–100`.

* Improved:
  * `upload-training-batch`
  * `train-hybrid-model` logic

* MongoDB stores:
  * `userID`, `average_vector`, `risk_score`, `timestamp`

---

## Features Implemented

* User signup and secure passcode storage
* Panic gesture configuration and decoy home screen
* OTP-based TPIN generation and biometric login
* **✅ Training Module for behavioral learning**
* **✅ Real-Time Continuous Monitoring**
* **✅ ML-based Risk Score Evaluation**
* Track Your Behavior screen
* Customize Risk Behavior screen
* Passcode change with OTP

---

## Upcoming Features

* Anomaly-based reauthentication logic (in progress)
* Risk-based session locking
* Dynamic UI adaptation based on score
* Admin portal to visualize analytics

---

## Setup Instructions

### Prerequisites

* Android Studio (latest)
* JDK 8+
* MongoDB Atlas
* Python 3.8+ with Flask
* Android Device/API 24+

---

## How to Setup and Run

### Step 1: Clone the Repository

```bash
git clone https://github.com/ananyadhagat/surakshak-Hackorbit.git
```

### Step 2: Backend Setup (Node.js and MongoDB)

```bash
cd surakshak-backend
node server.js
```

* Ensure your MongoDB URI is correctly configured inside `server.js` or in a `.env` file.
* Confirm that IP access is enabled for your development machine in MongoDB Atlas.
* Make sure the backend server is running before launching the Android client.

### Step 3: Android Application Setup

* Open the project folder in Android Studio.
* Connect an Android device or start an emulator.
* Adjust base API URLs in the Android app to match your backend server IP or use a tunneling service like ngrok for testing, or use your local device IP address in the API URLs.
* Build and run the application.

---

##

Now behavioural recognition , training session , continuous behavioral monitoring and reauthentication feature according to risk score are in processed state.

