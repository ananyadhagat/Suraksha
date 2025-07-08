# Behavior-Based Authentication System — Suraksha

##  Overview

**Suraksha** is a secure mobile authentication system that uses **behavioral biometrics** as a reauthentication mechanism. It enhances traditional login methods (passcode, biometric) with personalized behavior patterns to offer silent, non-intrusive, and adaptive security. It also includes a **panic gesture** feature to silently trigger emergency actions in the event of coercion or device theft.

---

##  Application Flow

### 1. Signup Screen
- Users register by entering their basic credentials (Phone number and name ).
-   After that otp is sent to user's system and then verification of otp is processed and if it succeeds then user is directed to set passcode otherwise if otp is wrong then user has to again signup.

### 2. Set Passcode
- A secure 6-digit passcode is set by the user.
- Passcode is stored securely in mongodb database after verification of otp again sent to mobile number of user to ensure that user has set the passcode.
- Passcode will be stored in database in encrypted form to ensure security of user.

### 3. Choose Panic Gesture
- User is prompted to **set a panic gesture** dialog box
- This gesture silently triggers a security protocol (e.g., logout, data wipe, alert) if used in distress.
- After he chooses the gesture he is directed to homescreen and now we have to integrate panic gestures to the required screens.

### 4. Home Screen
- Upon successful authentication, the user is redirected to the home dashboard.
- Currently implemented:
  - Display of user information or app options.
  - **Logout functionality** available here.
- **Upcoming:** Panic gesture triggers and behavior tracking modules will be enabled in this screen.

### 5. Login Screen
- User can log in via:
  - **Passcode**
  - **Biometric (Fingerprint)**
  - Forgot passcode functionality also enabled


---

## ⚙️ Features Till Now 
- **SignUp Functionality enabled**
- **Passcode setting**
-  **Panic Gesture Activation**
- **Logout Functionality on Home Screen**
- **Passcode + Biometric Login**

---

## 🚀 Setup Instructions

### 🛠 Prerequisites
- Android Studio (latest version)
- Java 8 or above
- MongoDB Atlas
- Node.js
- Emulator or Android Device (API 24+)

### 📦 Dependencies (Frontend)
- Volley (for API calls)
- BiometricPrompt API
- SharedPreferences (for local session management)

## 🚀 How to Run

1. Clone this repository:
   ```bash
   git clone (https://github.com/ananyadhagat/surakshak-Hackorbit.git)
2. Run the backend by navigating to the surakshak-backend folder
  ```bash
   cd surakshak-backend
   node server.js


## 📝 Setup Instructions

1. Clone the repository.
2. Open it in Android Studio.
3. Run on an emulator or connected device.

---
More features like panic gestures activation , behavioural recognition , training session to train ml model and more features to be added soon
