# Behavior-Based Authentication System — SURAKSHAK

## Overview

**Surakshak** is a secure mobile authentication system that extends beyond conventional security approaches by integrating behavioral biometrics, panic gestures, and simulated (fake) environments. These elements work together to detect unauthorized access and silently safeguard sensitive user data.

The system is engineered to offer continuous, adaptive, and covert protection by identifying and learning the unique interaction patterns of the legitimate user. It then reacts accordingly in scenarios involving coercion, device theft, or impersonation.

---

## Application Flow

### 1. Signup Screen

* The user begins registration by providing a name and mobile number.
* An OTP (One-Time Password) is sent to the provided mobile number.
* Upon successful OTP verification, the user is guided to set a secure passcode.
* If OTP verification fails, the user is required to retry the signup process.

### 2. Set Passcode

* The user creates a six-digit numeric passcode.
* This passcode is securely encrypted and stored in a MongoDB database.
* Before storage, a second OTP is sent to the user’s mobile number to reconfirm authenticity.
* Data handling complies with end-to-end security standards.

### 3. Choose Panic Gesture

* The user is prompted to select a panic gesture from available options (e.g., Panic Gesture 1 or 2).
* Upon selection, an instruction screen appears describing the chosen panic gesture and its implications.
* Panic gestures are designed to silently activate emergency actions such as:

  * Logging out of the session
  * Displaying a decoy (fake) home screen that replicates the interface but lacks functionality

**Summary of Panic Gestures:**

* **Panic Gesture 1:**

  * Login Screen: Enter the last six digits of your registered mobile number instead of your passcode.
  * Home Screen: Tap the template box above the Pay and Transfer section twice, then hold for 3 seconds on the third tap.
  * TPIN Entry: Tap the TPIN field twice, then hold on the third tap for 3 seconds.

* **Panic Gesture 2:**

  * Login Screen: Enter the first six digits of your registered mobile number instead of your passcode.
  * Home Screen: Hold the notification icon for 5 seconds.
  * TPIN Entry: Tap and hold the TPIN field for 5 seconds.

Triggering either gesture will silently lock the app for 10 minutes.

After configuration, the user is directed to the home screen.

### 4. Home Screen

* Following successful registration, the user is redirected to the main application dashboard.
* Current features on the home screen include:

  * Display of user details and functional options
  * Logout capability
  * Activation of a fake home screen upon panic gesture trigger, which includes automatic logout
  * Display of the Send Money interface
  * Integration and support for Privacy Gestures 1 and 2 as configured by the user
  * Creation of TPIN (Transaction PIN) enabled , user has to enter OTP firstly to create TPIN and then he can crete TPIN and store it in database in encrypted form.

### 5. Login Screen

* Users can authenticate through the following methods:
  * Passcode entry
  * Biometric verification via fingerprint using the BiometricPrompt API
  * Passcode recovery through the Forgot Passcode option using OTP verification

---

## Features Implemented

* User signup with OTP-based verification
* Secure passcode setup with encryption and double OTP confirmation
* Panic gesture configuration and instruction flow
* Panic Gesture Triggering also enabled
* Creation Of Transaction pin enabled 
* Authentication via passcode and biometric recognition
* Logout functionality
* Forgot Passcode workflow with OTP recovery

---

## Upcoming Features

* Behavioral Pattern Recognition — integration of machine learning models to detect user-specific behavioral traits
* Training Module — an interactive session to allow users to train the system with their typing and touch behavior
* Continuous Behavioral Monitoring — real-time tracking post-login to calculate a dynamic risk score and detect anomalies
* Reauthentication on the basis of Risk score generated

---

## Setup Instructions

### Prerequisites

* Android Studio (latest version recommended)
* Java Development Kit (JDK) 8 or higher
* Node.js (Latest Long-Term Support version)
* MongoDB Atlas account with proper network access permissions
* An Android emulator or a physical Android device with API level 24 or above

### Frontend Dependencies

* Volley: Handles REST API communication
* BiometricPrompt API: Enables fingerprint-based biometric authentication
* SharedPreferences: Manages session and local configuration data

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
* Adjust base API URLs in the Android app to match your backend server IP or use a tunneling service like ngrok for testing or using your device IP address in place of IP address In API URL
* Build and run the application.

More features like panic behavioural recognition , training session to train ml model and reauthentication and more homescreen features enabling are in progress
