# Route Tracker — Design Document

This document describes the architectural design and the role of each source file in the Route Tracker project.

## Architecture Overview

The project follows a modular architecture organized by concern, adhering to the principles of simplicity, testability, and clear intent.

- **Domain Layer**: Contains the core business logic and data models. It is independent of any Android-specific libraries (where possible).
- **Data Layer**: Defines abstractions for persistence (SessionStore) and location updates (LocationSource).
- **Repository**: Acts as the single source of truth, mediating between the UI and data layers.
- **UI Layer (Compose)**: Uses Jetpack Compose for a modern, declarative UI. It includes specialized components like the OpenStreetMap wrapper.

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

### Data Layer
- **`LocationSource.kt`**:
  - **Role**: Interface abstracting location hardware.
  - **Responsibility**: Provides a `Flow` of `TrackPoint` updates.
- **`SessionStore.kt`**:
  - **Role**: Interface abstracting persistence.
  - **Responsibility**: Provides methods to save, load, and observe tracking sessions.

### Repository Layer
- **`TrackingRepository.kt`**:
  - **Role**: The single source of truth and "brain" of the application.
  - **Responsibility**:
    - Manages the tracking lifecycle (start, pause, resume, stop).
    - Collects locations and updates the current session.
    - Ensures illegal state transitions are blocked.
    - Persists points incrementally to prevent data loss.

### UI Layer
- **`SessionViewModel.kt`**:
  - **Role**: UI state owner that survives configuration changes.
  - **Responsibility**:
    - Exposes `SessionUiState` to the view.
    - Translates user actions (start, stop) into repository calls.
    - Catches and exposes errors (e.g., invalid state transitions).
    - Merges domain session data with UI-only state (like transient errors).
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
- **`FakeLocationSource.kt`**:
  - **Role**: A test double for `LocationSource`.
  - **Responsibility**: Emits a predefined list of points as a flow.
- **`FakeSessionStore.kt`**:
  - **Role**: A test double for `SessionStore`.
  - **Responsibility**: Simulates an in-memory database for session persistence.

---

## Key Technologies
- **Jetpack Compose**: UI framework.
- **osmdroid**: OpenStreetMap library for Android.
- **Kotlin Coroutines**: (Planned) For asynchronous operations like database access and location streaming.
- **Hilt**: (Planned) For dependency injection.
- **Room**: (Planned) For local session persistence.
