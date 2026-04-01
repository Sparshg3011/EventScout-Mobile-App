<div align="center">

# EventScout

**Discover events, search artists, save favorites — all from a native Android app.**

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Node.js](https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=node.js&logoColor=white)](https://nodejs.org/)
[![Express](https://img.shields.io/badge/Express-000000?style=for-the-badge&logo=express&logoColor=white)](https://expressjs.com/)
[![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)

[Features](#features) · [Architecture](#architecture) · [Quick Start](#quick-start) · [API Reference](#api-reference) · [Contributing](#contributing)

</div>

---

## About

EventScout is a full-stack event discovery platform with a Node.js/Express backend and a native Android app built with Kotlin and Jetpack Compose. Users can browse curated events, search by keyword or artist, view venue and date details, save favorites, and get location-based recommendations — with optional Spotify integration for artist previews.

---

## Features

| Feature | Description |
|:--------|:------------|
| **Event Discovery** | Browse a curated feed of events on the Home screen |
| **Search** | Find events or artists by keyword with real-time results |
| **Event Details** | View venue, date, artist info, and ticket availability |
| **Favorites** | Save events to a personal favorites list for quick access |
| **Spotify Integration** | View artist profiles and listen to track previews |
| **Location-Based Recommendations** | Get nearby event suggestions using device GPS |

---

## Architecture

```mermaid
graph TB
    subgraph Android ["Android App — Kotlin / Jetpack Compose"]
        A[Home Screen<br/>Event Feed]
        B[Search Screen]
        C[Event Details]
        D[Favorites Screen]
        E[ViewModel Layer<br/>MVVM]
        F[Retrofit Client]
    end

    subgraph Backend ["Backend — Node.js / Express / TypeScript"]
        G[API Routes]
        H[Event Service]
        I[Search Service]
        J[Favorites Service]
    end

    subgraph Data ["Data Layer"]
        K[(MongoDB)]
        L[Spotify API]
        M[Google Play<br/>Location Services]
    end

    A & B & C & D --> E
    E --> F
    F -->|HTTP / JSON| G
    G --> H & I & J
    H & I & J --> K
    C -->|Artist Lookup| L
    A -->|Nearby Events| M

    style Android fill:#0a0a0a,stroke:#7F52FF,stroke-width:2px,color:#fff
    style Backend fill:#0a0a0a,stroke:#339933,stroke-width:2px,color:#fff
    style Data fill:#0a0a0a,stroke:#47A248,stroke-width:2px,color:#fff
```

### Request Flow

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ Compose  │────▶│ ViewModel│────▶│ Retrofit │────▶│ Express  │
│ UI       │     │ (MVVM)   │     │ HTTP     │     │ → Mongo  │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
```

---

## Quick Start

### Prerequisites

- **Node.js** 14+
- **MongoDB** (local or Atlas)
- **Android Studio** Koala or newer

### 1. Backend Setup

```bash
cd backend
npm install
```

Create `backend/.env`:

```env
PORT=3000
MONGO_URI=mongodb://localhost:27017/eventscout
```

```bash
npm run dev          # → http://localhost:3000
```

### 2. Android App Setup

1. Open **Android Studio** and select the `android` folder
2. Let Gradle sync and download dependencies
3. Configure the API base URL in `EventFinderService.kt`:
   - Emulator: `http://10.0.2.2:3000/api/`
   - Physical device: `http://<your-local-ip>:3000/api/`
4. Run on an emulator or connected device

---

## Project Structure

```
EventScout/
├── android/                        # Native Android App
│   ├── app/
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/.../
│   │   │       │   ├── network/   # Retrofit service definitions
│   │   │       │   ├── model/     # Data classes
│   │   │       │   ├── viewmodel/ # MVVM ViewModels
│   │   │       │   └── ui/        # Compose screens & components
│   │   │       └── res/           # Drawables, strings, themes
│   │   └── build.gradle.kts       # App-level build config
│   └── build.gradle.kts           # Project-level build config
│
└── backend/                        # Node.js / Express API
    ├── src/
    │   ├── database/              # MongoDB connection logic
    │   └── routes/                # API route handlers
    ├── package.json
    └── tsconfig.json
```

---

## Tech Stack

### Backend

| Layer | Technology |
|:------|:-----------|
| Runtime | Node.js |
| Framework | Express.js |
| Language | TypeScript |
| Database | MongoDB (Mongoose) |
| Config | dotenv |
| Dev Tooling | ts-node-dev, cors |

### Android App

| Layer | Technology |
|:------|:-----------|
| Language | Kotlin |
| UI | Jetpack Compose (Material3) |
| Architecture | MVVM |
| Networking | Retrofit 2 + Gson |
| Image Loading | Coil |
| Navigation | Jetpack Navigation Compose |
| Location | Google Play Services Location |

---

## Contributing

1. Fork the repository
2. Create your feature branch → `git checkout -b feat/amazing-feature`
3. Commit your changes → `git commit -m "feat: add amazing feature"`
4. Push to the branch → `git push origin feat/amazing-feature`
5. Open a Pull Request

---

## License

See [LICENSE](LICENSE) for details.

---

<div align="center">

**[Back to Top](#eventscout)**

</div>
