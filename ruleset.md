# Route Tracker — AI Implementation Guide

## Purpose

This document is a concise, authoritative reference for an AI assistant implementing an Android route-tracking app. Follow it closely to satisfy the project's quality rules: simplicity, testability, clear intent, and no duplicated logic.

---

## Quality Rules (always apply)

| Rule | Implication |
|------|-------------|
| **Simple** | Prefer the obvious solution. No extra layers, no premature abstractions. |
| **Tested** | Every non-trivial piece of logic must have a unit test. UI state changes included. |
| **Clear intent** | Names, types, and structure should make the "why" obvious without comments. |
| **No duplication** | One source of truth per concept. Derive, don't copy. |

---

## Feature Scope

- Create a tracking **session** identified by its start timestamp.
- **Start**, **pause/resume**, and **stop** tracking within a session.
- Record GPS points while tracking is active (not while paused).
- Calculate and display total **distance** in real-time.
- Display the recorded route as a polyline on an **OpenStreetMap** tile map.
- Persist sessions (metadata and points) so they survive process death.
- View a **history** list of all past sessions.

---

## Architecture

```
UI (Compose)
  └─ SessionViewModel          # UI state owner; survives rotation
       └─ TrackingRepository   # single source of truth for domain state
            ├─ TrackingService # foreground keep-alive
            ├─ LocationSource  # abstraction over FusedLocationProvider
            └─ SessionStore    # abstraction over persistence (Room)
```

**Rules:**
- The ViewModel holds *only* UI state derived from the repository. No business logic.
- The Repository owns all business rules (e.g. "calculate distance incrementally").
- Interfaces (`LocationSource`, `SessionStore`) isolate I/O so tests use fakes.

---

## Domain Model

```kotlin
/** Immutable snapshot of a single GPS fix. */
data class TrackPoint(val lat: Double, val lon: Double, val timestampMs: Long)

/** All states a session can be in. */
enum class TrackingState { IDLE, TRACKING, PAUSED, STOPPED }

/** Full session; the single source of truth persisted to the DB. */
data class TrackingSession(
    val id: Long = 0,
    val startedAt: Long? = null,
    val stoppedAt: Long? = null,
    val state: TrackingState = TrackingState.IDLE,
    val points: List<TrackPoint> = emptyList(),
    val distanceMeters: Double = 0.0,
)
```

**Distance Calculation**: Use the Haversine formula in `TrackPoint.distanceTo(other)` to ensure accuracy on a sphere.

---

## State Transitions

```
IDLE ──start()──► TRACKING ──pause()──► PAUSED
                     ▲                     │
                     └──────resume()───────┘
                     │
                  stop()
                     │
                     ▼
                  STOPPED
```

Enforce in the domain layer:

```kotlin
fun TrackingSession.withState(next: TrackingState): TrackingSession {
    val allowed = mapOf(
        IDLE     to setOf(TRACKING),
        TRACKING to setOf(PAUSED, STOPPED),
        PAUSED   to setOf(TRACKING, STOPPED),
        STOPPED  to emptySet(),
    )
    require(next in allowed.getValue(state)) { "Invalid transition: $state → $next" }
    return copy(state = next)
}
```

---

## TrackingRepository

```kotlin
class TrackingRepository(
    private val store: SessionStore,
    private val locationSource: LocationSource,
    private val scope: CoroutineScope,
    private val context: Context,
) {
    private val _session = MutableStateFlow<TrackingSession?>(null)
    val session: StateFlow<TrackingSession?> = _session.asStateFlow()

    suspend fun start() = transition(TRACKING) {
        _session.update { it?.copy(startedAt = System.currentTimeMillis()) }
        startService() // TrackingService (Foreground)
        startCollecting()
    }

    private fun startCollecting() {
        locationJob = scope.launch {
            locationSource.locations.collect { point ->
                _session.update { session ->
                    val newDist = session.points.lastOrNull()?.distanceTo(point) ?: 0.0
                    session?.copy(
                        points = session.points + point,
                        distanceMeters = session.distanceMeters + newDist
                    )
                }
                _session.value?.let { store.save(it) }
            }
        }
    }

    private suspend fun transition(next: TrackingState, sideEffect: suspend () -> Unit = {}) {
        val updated = requireSession().withState(next)
        sideEffect()
        val final = _session.value?.copy(state = next) ?: updated
        _session.value = store.save(final)
    }
}
```

---

## UI (Jetpack Compose)

### Screen structure

```
RouteTrackerScreen
  ├─ if activeSession == null → SessionList(allSessions)
  └─ else
       ├─ SessionHeader(startedAt, stoppedAt, distance)
       ├─ OsmMapView(points, showMarker)
       └─ TrackingControlsBar(state)
```

### Formatting (single location)

```kotlin
// SessionHeader.kt
private val timestampFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
fun Long?.toDisplayTime(): String = this?.let { timestampFormatter.format(Date(it)) } ?: "—"

// SessionList.kt
fun Double.toDisplayDistance(): String = if (this >= 1000) "%.2f km".format(this/1000) else "%.0f m".format(this)
```

---

## Testing Strategy

### Selective Test Runner

To protect real user data on physical devices, use a custom `AndroidJUnitRunner` that only executes on emulators:

```kotlin
class SelectiveTestRunner : AndroidJUnitRunner() {
    override fun onCreate(arguments: Bundle?) {
        if (!isEmulator) {
            val newArgs = arguments ?: Bundle()
            newArgs.putString("notPackage", "com.katharina.routetracker")
            super.onCreate(newArgs)
        } else {
            super.onCreate(arguments)
        }
    }
}
```

---

## Implementation Checklist

- [x] Domain model + `withState()` + distance calculation
- [x] `toControlsState()` + unit tests
- [x] `FakeLocationSource` and `FakeSessionStore`
- [x] `TrackingRepository` + unit tests (incremental distance + persistence)
- [x] Room entities, DAO, converter, `RoomSessionStore`
- [x] `FusedLocationSource` (3m threshold, 5s interval)
- [x] Hilt module wiring
- [x] `SessionViewModel` + `toUiState()` + unit tests
- [x] Navigation: `SessionList` ↔ `DetailView` with `BackHandler`
- [x] `OsmMapView` / `TrackingControlsBar` / `SessionHeader`
- [x] `TrackingService` (foreground + notifications)
- [x] `SelectiveTestRunner` for safe testing
- [x] Smoke UI tests for all components
