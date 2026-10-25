# Smart Water Tracker 💧

Smart Water Tracker is a clean, modern, student-friendly Android application designed to help users monitor their daily hydration intake, track their progress towards a personal goal, and set automatic reminders to drink water throughout the day.

Built using **Java** and **XML layouts**, this app follows native Android development best practices and is ideal for BCA / CS college project demonstrations.

---

## 📋 Table of Contents

- [Overview & Purpose](#-overview--purpose)
- [Key Features](#-key-features)
- [Technologies Used](#-technologies-used)
- [App Architecture & How It Works](#-app-architecture--how-it-works)
- [Project Folder Structure](#-project-folder-structure)
- [Prerequisites](#-prerequisites)
- [Installation & Setup Guide](#-installation--setup-guide)
- [Building & Running the App](#-building--running-the-app)
- [User Guide](#-user-guide)
- [Notification Permissions & Limitations](#-notification-permissions--limitations)
- [Testing Instructions](#-testing-instructions)
- [Future Improvements](#-future-improvements)
- [Author](#-author)
- [License](#-license)

---

## 🎯 Overview & Purpose

Drinking sufficient water daily is essential for maintaining health, energy, and concentration. However, many people forget to hydrate regularly during busy study or work hours. 

**Smart Water Tracker** solves this by providing:
1. **Easy Water Tracking**: Quick-add buttons to record water consumption instantly.
2. **Visual Progress**: Dynamic progress bar and percentage display indicating proximity to the daily goal.
3. **Automatic Reset**: Fresh daily record starting automatically each new calendar day.
4. **Timely Reminders**: Local scheduled notifications alerting users to take a water break every ~2 hours.

---

## ✨ Key Features

- 💧 **Quick Add Water**: Instant buttons to log `+250 ml` and `+500 ml` with immediate UI updates.
- 🎯 **Customizable Daily Goal**: Default goal set to 2000 ml, with an option to set a custom goal (e.g., 2500 ml, 3000 ml) via a dialog.
- 📊 **Visual Progress Tracking**: Real-time horizontal progress bar and percentage display.
- 🔄 **Safe Reset Option**: Reset button with a confirmation alert dialog to prevent accidental deletion.
- 📅 **Automatic Daily Rollover**: Automatically resets daily intake to 0 ml when the local calendar date changes (`yyyy-MM-dd`).
- 🔔 **Scheduled Water Reminders**: Repeating local alarms (~every 2 hours) using Android `AlarmManager` and `BroadcastReceiver`.
- ⚡ **Device Reboot Support**: Automatically restores scheduled reminders after a phone restart (`RECEIVE_BOOT_COMPLETED`).
- 🛡️ **Android 13+ Notification Support**: Handles runtime notification permission (`POST_NOTIFICATIONS`) gracefully.
- 🎨 **Modern Blue-and-White UI**: Styled using Material 3 card components (`MaterialCardView`) inside a scrollable layout.

---

## 🛠️ Technologies Used

- **Programming Language**: Java (JDK 11)
- **User Interface**: Android XML Layouts, Material Design 3 Components
- **Persistence**: Android `SharedPreferences` (Key-Value local storage)
- **Scheduling**: `AlarmManager` (Inexact repeating alarm for battery conservation)
- **Background Events**: `BroadcastReceiver` (`WaterReminderReceiver`)
- **System Notifications**: Android `NotificationChannel` & `NotificationCompat`
- **Build System**: Gradle 8.x with Android Gradle Plugin (AGP) 8.x/9.x
- **Target SDK**: Android 16 (API 37), **Min SDK**: API 24 (Android 7.0+)

---

## ⚙️ App Architecture & How It Works

### 1. Data Storage (`SharedPreferences`)
All user data is stored locally using `SharedPreferences` (`WaterTrackerPrefs`):
- `key_water_intake`: Stores today's total water intake in ml (integer).
- `key_daily_goal`: Stores user's daily hydration target (integer, default `2000`).
- `key_last_date`: Stores the date of the last recorded intake (`"yyyy-MM-dd"` string).
- `key_reminder_enabled`: Stores whether reminders are enabled (boolean).

### 2. Daily Date Rollover Logic
When the app launches, `MainActivity` compares today's calendar date with `key_last_date`:
- If `currentDate` equals `savedDate`, previous intake for the day is loaded.
- If `currentDate` does NOT match `savedDate` (a new calendar day has started), `currentIntake` is set to `0 ml`, and the date is updated automatically.

### 3. Alarm Scheduling & Notifications
- When the reminder switch is turned **ON**:
  - Runtime notification permission (`POST_NOTIFICATIONS`) is checked on Android 13+ (API 33+).
  - `WaterReminderReceiver.scheduleReminder()` sets a repeating alarm using `AlarmManager.setInexactRepeating()`.
- When the alarm triggers (~every 2 hours):
  - `WaterReminderReceiver.onReceive()` fires and creates a system notification channel (`water_reminder_channel`).
  - Displays a notification with the title *"Time to Drink Water! 💧"* and pending intent to open `MainActivity`.
- On phone reboot:
  - The system sends `android.intent.action.BOOT_COMPLETED`.
  - `WaterReminderReceiver` catches this action and re-schedules the alarm if reminders were enabled before reboot.

---

## 📁 Project Folder Structure

```text
SmartWaterTracker/
├── app/
│   ├── build.gradle.kts                # Application build configuration & dependencies
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml      # App permissions, activities & broadcast receivers
│           ├── java/com/example/smartwatertracker/
│           │   ├── MainActivity.java    # Main UI logic, calculations, date rollover & event handlers
│           │   └── WaterReminderReceiver.java # Receiver for alarm triggers, notifications & boot restore
│           └── res/
│               ├── drawable/
│               │   └── ic_water_drop.xml # Water drop vector graphic
│               ├── layout/
│               │   └── activity_main.xml # Primary layout XML with Material Cards & Controls
│               └── values/
│                   ├── colors.xml       # Water theme color definitions
│                   ├── strings.xml      # UI text constants & string formats
│                   └── themes.xml       # Material 3 application theme settings
├── build.gradle.kts                    # Root build script
├── settings.gradle.kts                 # Module settings
├── gradle.properties                   # Gradle environment configuration
└── README.md                           # Project documentation
```

---

## 📌 Prerequisites

Before running the project, make sure you have installed:
- **Android Studio**: Jellyfish / Koala / Ladybug / Meerkat or newer.
- **JDK**: Java Development Kit 11 or higher (included with Android Studio).
- **Android SDK**: API level 34 or higher installed via Android Studio SDK Manager.
- **Test Device / Emulator**: Android phone running Android 7.0 (API 24) or higher.

---

## 🚀 Installation & Setup Guide

### Step 1: Clone the Repository
Open terminal or command prompt and run:
```bash
git clone https://github.com/sudhanshuyaduwansi/SmartWaterTracker.git
```

### Step 2: Open in Android Studio
1. Launch **Android Studio**.
2. Click **Open** (or **File > Open**).
3. Navigate to the cloned `SmartWaterTracker` directory and select it.
4. Wait for Android Studio to sync Gradle dependencies automatically.

---

## 🛠️ Building & Running the App

### Running on a Physical Device / Emulator:
1. Connect an Android smartphone via USB with **USB Debugging** enabled in Developer Options, OR start an AVD Emulator.
2. Select your device from the top target device dropdown menu in Android Studio.
3. Click the green **Run** button (or press `Shift + F10`).

### Generating a Debug APK:
1. Go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Android Studio will compile the app.
3. Click **locate** in the popup to find `app-debug.apk` in `app/build/outputs/apk/debug/`.

---

## 📱 User Guide

| Action | How to Perform | What Happens |
| :--- | :--- | :--- |
| **Log +250 ml** | Click **+ 250 ml** button | Increases total intake by 250 ml, updates progress bar & percentage immediately. |
| **Log +500 ml** | Click **+ 500 ml** button | Increases total intake by 500 ml, updates progress bar & percentage immediately. |
| **Change Goal** | Click **Set Daily Goal** | Opens a number dialog. Enter new target in ml (e.g., `2500`) and click **Save**. |
| **Reset Intake** | Click **Reset Today's Intake** | Prompts confirmation alert dialog. Clicking **Reset** sets intake back to `0 ml`. |
| **Enable Reminders**| Toggle **Water Reminders** switch **ON** | Requests notification permission (API 33+) and schedules ~2-hour repeating reminders. |
| **Disable Reminders**| Toggle **Water Reminders** switch **OFF** | Cancels scheduled alarms in `AlarmManager` and stops notifications. |

---

## ⚠️ Notification Permissions & Limitations

1. **Android 13+ (API 33+) Runtime Permission**:
   - The app explicitly requests `POST_NOTIFICATIONS` permission when the user toggles reminders ON.
   - If permission is denied, reminders remain turned OFF gracefully with an explanatory Toast message.

2. **Battery Optimization & Doze Mode**:
   - The app utilizes `AlarmManager.setInexactRepeating` to comply with Android battery optimization policies.
   - On Android 6.0+, Doze Mode may group alarm deliveries together, so exact timing may vary by a few minutes depending on device state.

---

## 🧪 Testing Instructions

### 1. Water Tracking & Persistence Test:
1. Open the app and click `+ 250 ml` twice (Total: `500 / 2000 ml`).
2. Close the app completely from Recent Apps.
3. Reopen the app. Verify that intake remains `500 / 2000 ml` (25%).

### 2. Goal Changing & Reset Test:
1. Click **Set Daily Goal** and enter `1000`. Click **Save**.
2. Observe that percentage updates automatically (`500 / 1000 ml` = 50%).
3. Click **Reset Today's Intake** and confirm. Verify intake returns to `0 / 1000 ml` (0%).

### 3. Notification Scheduling Test:
1. Toggle **Water Reminders** switch ON.
2. Accept notification permission prompt if on Android 13+.
3. Verify Toast message *"Water reminders scheduled every 2 hours!"* appears and reminder status updates to *"Reminders Enabled (Every 2 Hours)"*.

---

## 🔮 Future Improvements

- [ ] **Hydration History & Charts**: Weekly/monthly graphical logs using MPAndroidChart.
- [ ] **Custom Container Sizes**: Allow users to add custom amounts (e.g. 150 ml, 350 ml).
- [ ] **Sound Effects**: Play subtle water drop sounds on adding water.
- [ ] **Home Screen Widget**: Quick-add widget directly on the device home screen.

---

## 👤 Author

**Sudhanshu Raj**
- GitHub: [@sudhanshuyaduwansi](https://github.com/sudhanshuyaduwansi)
- Project: Smart Water Tracker (Android College Project)

---

## 📄 License

*No license specified.* All rights belong to the author. You may use this repository for educational, learning, and academic project evaluation purposes.
