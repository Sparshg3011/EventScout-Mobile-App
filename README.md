# EventScout Mobile App

EventScout is a comprehensive event discovery platform featuring a robust backend and a modern native Android application. Users can discover events, search for specific activities, view detailed information, and manage their favorite events.

## Features

- **Event Discovery**: Browse a curated list of events on the Home screen.
- **Search**: Find specific events or artists using the Search screen.
- **Event Details**: View detailed information about events, including venue, date, and artist details.
- **Favorites**: Save events to your favorites list for easy access.
- **Spotify Integration**: View artist details and potentially listen to previews (if authorized).
- **Location Services**: Get event recommendations based on your location.

## Tech Stack

### Backend

- **Framework**: Node.js with Express.js (TypeScript)
- **Database**: MongoDB (via Mongoose)
- **Tools**:
  - `dotenv` for environment configuration
  - `cors` for cross-origin resource sharing
  - `ts-node-dev` for development

### Android App

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose (Material3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit 2 + Gson
- **Image Loading**: Coil
- **Navigation**: Jetpack Navigation Compose
- **Location**: Google Play Services Location

## Prerequisites

- **Node.js**: v14 or higher
- **npm** or **yarn**
- **Android Studio**: Koala or newer (recommended)
- **MongoDB**: Local instance or Atlas URI (optional but recommended for full functionality)

## Getting Started

### Backend Setup

1.  Navigate to the `backend` directory:
    ```bash
    cd backend
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Create a `.env` file in the `backend` directory with the following variables (example):
    ```env
    PORT=3000
    MONGO_URI=mongodb://localhost:27017/eventscout
    ```
4.  Start the development server:
    ```bash
    npm run dev
    ```
    The server will start on `http://localhost:3000`.

### Android App Setup

1.  Open **Android Studio**.
2.  Select **Open** and verify functionality by navigating to the `android` folder in this project (select the `android` folder, not `app`).
3.  Allow Gradle to sync and download dependencies.
4.  Create a `local.properties` file in the `android` root if it doesn't exist (Android Studio usually handles this) and ensure your `sdk.dir` is pointed to your Android SDK location.
5.  **Configure API Endpoint**:
    - Open `android/app/src/main/java/com/example/eventfinder/network/EventFinderService.kt` (or wherever the base URL is defined).
    - Ensure the base URL points to your running backend. If running on an emulator, use `http://10.0.2.2:3000/api/`. If on a physical device, use your machine's local IP address (e.g., `http://192.168.1.x:3000/api/`).
6.  Run the app on an Emulator or connected device.

## Project Structure

```
EventScout--Mobile-App-/
├── android/            # Android Mobile Application
│   ├── app/            # Main App Module
│   │   ├── src/        # Source Code (Kotlin/Java)
│   │   └── res/        # Resources (XML, Images)
│   └── build.gradle.kts # Project-level Build Configuration
├── backend/            # Node.js/Express Backend
│   ├── src/            # Source Code (TypeScript)
│   │   ├── database/   # Database Connection Logic
│   │   └── routes/     # API Routes
│   ├── package.json    # Dependencies and Scripts
│   └── tsconfig.json   # TypeScript Configuration
└── README.md           # This file
```

## Contributing

1.  Fork the repository.
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.
