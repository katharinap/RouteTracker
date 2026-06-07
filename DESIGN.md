# Route Tracker — Design Document

This document describes the architectural design and the role of each source file in the Route Tracker project.

## Architecture Overview

The project follows a modular architecture organized by concern, adhering to the principles of simplicity, testability, and clear intent.

- **Domain Layer**: Contains the core business logic and data models. It is independent of any Android-specific libraries (where possible).
- **UI Layer (Compose)**: Uses Jetpack Compose for a modern, declarative UI. It includes specialized components like the OpenStreetMap wrapper.
- **Data Layer (Planned)**: Will handle persistence (Room) and location updates (Fused Location Provider).
- **Repository (Planned)**: Will act as the single source of truth, mediating between the UI and data layers.

---

## Source File Roles

### Domain Layer
- **`TrackPoint.kt`**: 
  - **Role**: A data class representing an immutable snapshot of a single GPS fix.
  - **Contents**: Latitude, longitude, and a timestamp in milliseconds.
- **`TrackingState.kt`**:
  - **Role**: Enum defining all possible session states (`IDLE`, `TRACKING`, `PAUSED`, `STOPPED`).
  - **Responsibility**: Provides `toControlsState()` to map internal state to UI button visibility.
- **`TrackingSession.kt`**:
  - **Role**: The aggregate root for a tracking activity.
  - **Responsibility**: Provides `withState()` to enforce legal state transitions.

### UI Layer
- **`OsmMapView.kt`**: 
  - **Role**: A Compose-friendly wrapper for the **osmdroid** library.
  - **Responsibilities**:
    - Initializes the `MapView`.
    - Updates the map overlays (e.g., polyline) when the list of points changes.
    - Handles map centering and zoom levels.
- **`MainActivity.kt`**:
  - **Role**: The main entry point of the application.
  - **Responsibilities**:
    - Configures the osmdroid user agent (required for tile downloads).
    - Sets up the Compose `Scaffold`.
    - Currently hosts the dummy data and the `OsmMapView` for demonstration.

### Testing
- **`OsmMapViewTest.kt` (AndroidTest)**:
  - **Role**: Instrumented UI tests for the map component.
  - **Responsibilities**:
    - Verifies that `OsmMapView` renders without crashing when provided with data.
    - Verifies that it handles empty data gracefully.

---

## Key Technologies
- **Jetpack Compose**: UI framework.
- **osmdroid**: OpenStreetMap library for Android.
- **Kotlin Coroutines**: (Planned) For asynchronous operations like database access and location streaming.
- **Hilt**: (Planned) For dependency injection.
- **Room**: (Planned) For local session persistence.
