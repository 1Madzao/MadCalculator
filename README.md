# MadCalculator

<div align="center">

[![Android Platform](https://img.shields.io/badge/Platform-Android_8.0+_(API_26--33)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Java 11 / Kotlin DSL](https://img.shields.io/badge/Language-Java_11_%2F_Kotlin_DSL-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM_%2B_LiveData_%2B_ViewBinding-00599C?style=for-the-badge&logo=google)](https://developer.android.com/topic/architecture)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)
[![CI Status](https://img.shields.io/badge/CI-Passing-brightgreen?style=for-the-badge&logo=github-actions&logoColor=white)](.github/workflows/android.yml)

<p align="center">
  Scientific calculator for Android with a custom math parser, glassmorphism UI, and real-time canvas particle background.
</p>

</div>

---

## Overview

**MadCalculator** is an open-source Android scientific calculator built in Java. It features a custom recursive descent parser, reactive MVVM architecture, and a real-time particle background rendered on Canvas.

It supports basic arithmetic, algebraic expressions, and trigonometric functions in Degree and Radian modes, with persistent calculation history.

---

## Features

### Mathematical Parser Engine (`CalculatorEngine`)
* **Recursive descent parser**: Parses arithmetic expressions, powers, roots, factorials, and functions without external math libraries or JS runtimes.
* **Operator precedence**: Standard order of operations (parentheses, exponents/roots, multiplication/division, addition/subtraction).
* **Implicit multiplication**: Supports expressions like `2(3)`, `2π`, `(2+3)(4)`, and `3sin(30)`.
* **Trigonometry**: `sin`, `cos`, `tan` with toggling between Degree (**DEG**) and Radian (**RAD**) modes.
* **Decimal precision**: Uses `BigDecimal` with rounding and `DecimalFormat` configured for localized formatting.
* **Fault-tolerant typing**: Automatically normalizes trailing operators and unclosed parentheses for real-time live preview.

### Architecture & UI
* **MVVM + LiveData**: Unidirectional data flow between `MainActivity` and `CalculatorViewModel`. State survives configuration changes.
* **ViewBinding**: Type-safe layout bindings without `findViewById`.
* **Glassmorphic UI**: Material 3 translucent surfaces and specular highlights.
* **Particle Background (`ParticleBackgroundView`)**: 36 autonomous particles simulated on Android Canvas with velocity vectors and HSV color cycles, pausing during Activity stop events.
* **Calculation History**: Up to 50 previous calculations stored in `SharedPreferences` via JSON (`HistoryManager`), accessible through a BottomSheet with one-tap recall.
* **Haptics**: Tactile feedback on key taps.

---

## Architecture Diagram

```
       ┌────────────────────────┐
       │      MainActivity      │
       │    (ViewBinding UI)    │
       └───────────┬────────────┘
                   │ Observes LiveData / Dispatches Actions
                   ▼
       ┌────────────────────────┐
       │  CalculatorViewModel   │
       │     (State & UDF)      │
       └───────────┬────────────┘
         ┌─────────┴─────────┐
         ▼                   ▼
┌──────────────────┐ ┌───────────────┐
│ CalculatorEngine │ │HistoryManager │
│  (Custom Parser) │ │(JSON Storage) │
└──────────────────┘ └───────────────┘
```

---

## Project Structure

```
MadCalculator/
├── .github/
│   └── workflows/
│       └── android.yml                # CI pipeline (build & test)
├── app/
│   ├── build.gradle.kts               # App module build configuration
│   ├── proguard-rules.pro             # Proguard rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/madstudio/madcalculator/
│       │   │   ├── adapter/
│       │   │   │   └── HistoryAdapter.java         # RecyclerView adapter for history
│       │   │   ├── data/
│       │   │   │   └── HistoryManager.java         # SharedPreferences JSON persistence
│       │   │   ├── model/
│       │   │   │   └── HistoryItem.java            # History data model
│       │   │   ├── viewmodel/
│       │   │   │   └── CalculatorViewModel.java    # Presentation logic and state
│       │   │   ├── CalculatorEngine.java           # Math parser
│       │   │   ├── MainActivity.java               # ViewBinding & UI interactions
│       │   │   └── ParticleBackgroundView.java     # Canvas particle system
│       │   └── res/                                # Layouts, styles, glassmorphic themes
│       └── test/
│           └── java/com/madstudio/madcalculator/
│               └── CalculatorEngineTest.java       # Unit tests
├── build.gradle.kts                   # Root build script
├── gradle.properties
├── gradlew
└── settings.gradle.kts
```

---

## Build & Test

### Prerequisites
* Android SDK (Compile SDK 33, Min SDK 26)
* JDK 11 or 17

### Command Line
* **Run unit tests:**
  ```bash
  ./gradlew test
  ```

* **Build debug APK:**
  ```bash
  ./gradlew assembleDebug
  ```
  Output: `app/build/outputs/apk/debug/app-debug.apk`

* **Install via ADB:**
  ```bash
  ./gradlew installDebug
  ```

### AndroidIDE (Mobile)
MadCalculator builds directly on Android with AndroidIDE:
1. Open the project folder in AndroidIDE (`/sdcard/AndroidIDEProjects/MadCalculator`).
2. Run the build or install task.

---

## CI Pipeline

GitHub Actions workflow at [`.github/workflows/android.yml`](.github/workflows/android.yml):
* Triggers on `push` and `pull_request` to `main` / `master`.
* Sets up JDK 17, runs `./gradlew test`, builds the APK, and uploads it as an artifact.

---

## License

MIT License. See [LICENSE](LICENSE) for details.
