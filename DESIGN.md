# Route Tracker — Design Document

This document describes the architectural design and the role of each source file in the Route Tracker project.

## Architecture Overview

The project follows a modular architecture organized by concern, adhering to the principles of simplicity, testability, and clear intent.

- **Domain Layer**: Contains the core business logic and data models. It is independent of any Android-specific libraries (where possible).
- **Data Layer**: Defines abstractions for persistence (SessionStore) and location updates (LocationSource).
- **Repository**: Acts as the single source of truth, mediating between the UI and data layers.
- **Service Layer**: Handles background operations to ensure tracking survives app backgrounding.
- **UI Layer (Compose)**: Uses Jetpack Compose for a modern, declarative UI.

---

## Source File Roles

### Domain Layer
- **`TrackPoint.kt`**: 
  - **Role**: A data class representing an immutable snapshot of a single GPS fix.
  - **Responsibility**: Provides `distanceTo()` using the Haversine formula for accurate distance calculation.
- **`TrackingState.kt`**:
  - **Role**: Enum defining all possible session states (`IDLE`, `TRACKING`, `PAUSED`, `STOPPED`).
  - **Responsibility**: Provides `toControlsState()` to map internal state to UI button visibility.
- **`TrackingSession.kt`**:
  - **Role**: The aggregate root for a tracking activity.
  - **Responsibility**: Manages session metadata (timestamps, distance) and provides `withState()` to enforce legal state transitions.

### Data Layer
- **`LocationSource.kt`**:
  - **Role**: Interface abstracting location hardware.
- **`FusedLocationSource.kt`**:
  - **Role**: Production implementation using Google Play Services.
  - **Responsibility**: Streams GPS updates with a 5s interval and 3m distance threshold.
- **`SessionStore.kt`**:
  - **Role**: Interface abstracting persistence.
- **`room/`**:
  - **`SessionEntity.kt`**: Database schema for tracking sessions (including distance and points JSON).
  - **`SessionDao.kt`**: SQL queries for session persistence.
  - **`AppDatabase.kt`**: Room database configuration with destructive migration for development.
  - **`TrackPointConverter.kt`**: Serializes `List<TrackPoint>` to JSON for storage.
  - **`RoomSessionStore.kt`**: Production implementation of `SessionStore` using Room.

### Repository Layer
- **`TrackingRepository.kt`**:
  - **Role**: The single source of truth and "brain" of the application.
  - **Responsibility**:
    - Manages the tracking lifecycle (start, pause, resume, stop).
    - Collects locations and updates the current session.
    - Calculates distance incrementally as new points are recorded.
    - Orchestrates the `TrackingService` lifecycle.

### Service Layer
- **`TrackingService.kt`**:
  - **Role**: A Foreground Service that keeps the tracking process alive.
  - **Responsibility**: Displays an ongoing notification and ensures the repository continues collecting points in the background.

### UI Layer
- **`RouteTrackerScreen.kt`**:
  - **Role**: The primary screen orchestrator.
  - **Responsibility**: Handles navigation between the session list and detail view, and manages location/notification permissions.
- **`SessionList.kt`**:
  - **Role**: Displays a list of all saved tracking sessions with their distance and start times.
- **`SessionViewModel.kt`**:
  - **Role**: UI state owner.
  - **Responsibility**: Exposes reactive state and translates user actions into repository calls.
- **`TrackingControlsBar.kt`**:
  - **Role**: Context-sensitive buttons for session control.
- **`SessionHeader.kt`**:
  - **Role**: Displays formatted timestamps and total distance.
- **`OsmMapView.kt`**: 
  - **Role**: Compose wrapper for **osmdroid**.

### Testing
- **`SelectiveTestRunner.kt`**:
  - **Role**: Custom Android Test Runner.
  - **Responsibility**: Prevents instrumented tests from running on real devices to protect user data.
- **`FakeLocationSource.kt` / `FakeSessionStore.kt`**:
  - **Role**: Test doubles for fast, reliable JVM unit testing.

---

## Key Technologies
- **Jetpack Compose**: UI framework.
- **osmdroid**: OpenStreetMap library for Android.
- **Kotlin Coroutines**: For asynchronous operations and reactive data streams (Flow).
- **Hilt**: Dependency injection.
- **Room**: Local session persistence.
- **KotlinX Serialization**: JSON serialization for GPS points.
