# MFS Fund & Expense Tracker 📱💸

A minimalist, utility-focused, production-grade Android financial utility built with **Kotlin** and **Jetpack Compose (Material 3)**. The application is designed for rapid daily reconciliation of Mobile Financial Services (bKash & Nagad) balances, real-time expense and income logging, automatic ledger calculations, local persistence with Room Database, and scoped external storage CSV export.

---

## 📸 Key Features & Architecture

- **Modern Jetpack Compose UI**: Clean card-based design with soft rounded corners (`16.dp`), high-contrast numeric inputs, branded MFS badges (bKash Rose, Nagad Orange, Agent Teal), and custom Material 3 typography.
- **Theme Support**: Seamless Light Mode and OLED Charcoal Dark Mode with a persistent toggle saved in Jetpack DataStore Preferences.
- **Fixed MFS Multi-Account Ledger**: Pre-configured structure for 7 specific accounts across 4 account groups with instant real-time summation.
- **Dynamic Transaction Logging**: Add or remove daily expense and income items on the fly with a 2-way segmented selector and dedicated split summary metrics.
- **Real-Time Reconciliation**: Part 3 Available Balance automatically mirrors Part 1 Total MFS Balances reactively, alongside persistent previous balance and MSA fields.
- **Persistence & Export**:
  - Local Room SQLite database storing full daily snapshots with JSON TypeConverters.
  - Zero-permission Scoped Storage CSV report generation (`Documents/MFS_Tracker/MFS_Tracker_YYYY-MM-DD_HHmm.csv`) via Android MediaStore (Android 10–15+) and Storage Access Framework (`Save As...`).
- **CI/CD Automation**: GitHub Actions workflow (`.github/workflows/android_build.yml`) for automated debug builds and APK artifact publishing.

---

## 📐 Application Structure & Walkthrough

The app dashboard is organized into three primary sections plus a sticky top navigation bar and bottom action sheet:

### 1. Header & Timestamp Bar
- **App Title & Branding**: Displays `"MFS Tracker"` alongside the current ledger mode (`Daily Ledger` or `Editing Saved Snapshot`).
- **Action Icons**:
  - 🌙 / ☀️ **Theme Toggle**: Instantly toggles between Light and Dark mode.
  - 🕒 **History & Export**: Opens the saved snapshots dialog with an active record counter badge.
- **Sub-Header Pills**:
  - `[ 📅 DD MMM YYYY ]`: Tap to open the standard Material 3 `DatePickerDialog`.
  - `[ 🕒 hh:mm a ]`: Tap to open the Material 3 `TimePickerDialog`.
  - `[ + New Sheet / Reset ]`: Clears the form for a new day while auto-carrying forward the previous record's Available Balance and MSA.

---

### 2. Part 1: MFS Account Balances
Card-based multi-account groups formatted with SIM indicators and formatted phone numbers (`01XXX-XXXXXX`):

| SL# | Account Number | MFS Provider | Key / Type |
|:---:|:--------------:|:------------:|:----------:|
| **1** | `01605-001886` | bKash & Nagad | Row 1A & 1B |
| **2** | `01965-972859` | bKash & Nagad | Row 2A & 2B |
| **3** | `01842-662901` | bKash & Nagad | Row 3A & 3B |
| **4** | `01715-385025` | bKash Agent | Row 4 (Agent) |

- **Total MFS Balance**: Real-time reactive sum displayed in a prominent highlighted banner (`৳ 0.00 BDT`).

---

### 3. Part 2: Expense & Income Log
Dynamic ledger items allowing swift tracking of daily cash movements:
- **Type Toggle**: 2-way segmented pill chip for `[ Expense ]` (soft coral red) and `[ Income ]` (emerald green).
- **Item Fields**:
  - `Description`: Item description (e.g., Office Supplies, Courier, Rent).
  - `Amount (৳)`: Numeric decimal input field with currency prefix.
  - `Notes (optional)`: Additional reference or recipient details.
- **Dynamic Controls**: `+ Add Item` tonal button to append rows, and delete trash icons per card.
- **Split Summary Footer**:
  - `Total Expenses`: Highlighted red summary card (`৳ 0.00`).
  - `Total Income`: Highlighted green summary card (`৳ 0.00`).

---

### 4. Part 3: Reconciliation & MSA Summary
Provides real-time business reconciliation:
1. **Available Balance**: Large highlighted card that strictly mirrors the **Part 1 Total MFS Balances** in real time.
2. **Previous Balance**: Numeric input auto-populated from the latest saved snapshot's Available Balance (manually editable).
3. **Current MSA**: Numeric input for today's MSA amount.
4. **Previous MSA**: Numeric input auto-populated from the latest saved snapshot's MSA (manually editable).

---

### 5. Bottom Actions & Export
- **`Save Record`**: Saves the complete snapshot into the local Room database.
- **`Save & Export CSV`**: Saves the snapshot and creates an RFC-4180 compliant CSV file saved to:
  ```text
  Documents/MFS_Tracker/MFS_Tracker_YYYY-MM-DD_HHmm.csv
  ```
- **History Dialog**: Allows viewing, loading into editor, deleting, and re-exporting previous records.

---

## 🛠️ Tech Stack & Dependencies

- **Language**: Kotlin 2.3+
- **UI Toolkit**: Jetpack Compose (BOM 2026.03.01) with Material 3
- **Architecture**: Clean Architecture / MVVM (`TrackerViewModel`, `TrackerRepository`, Room DAO)
- **Local Database**: AndroidX Room 2.8.5 with Kotlin Symbol Processing (KSP)
- **Serialization**: `kotlinx-serialization-json` 1.12.0
- **Preferences**: Jetpack DataStore Preferences 1.3.0
- **Target SDK**: Android 36 (Android 16) | **Min SDK**: Android 26 (Android 8.0 Oreo)

---

## 🚀 Building & Running

### Prerequisites
- JDK 17 or JDK 21
- Android SDK Platform 36 & Build-Tools 36+

### Build Production Release APK
```bash
./gradlew assembleRelease
```
The optimized and signed production APK is generated at:
```text
app/build/outputs/apk/release/app-release.apk
```

### Build Debug APK
```bash
./gradlew assembleDebug
```
The output debug APK is generated at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

---

## 📦 CI/CD Workflow

The repository includes an automated GitHub Actions workflow defined in [`.github/workflows/android_build.yml`](.github/workflows/android_build.yml). On every push or pull request to the `main` branch, the workflow:
1. Checks out the repository.
2. Sets up Temurin JDK 17 with Gradle caching.
3. Builds the project (`./gradlew assembleDebug`).
4. Uploads the compiled `app-debug.apk` as a build artifact retained for 14 days.

---

## 📄 License
This project is open-source and available under standard development licensing.

