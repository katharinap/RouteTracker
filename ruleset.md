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
- Display the recorded route as a polyline on an **OpenStreetMap** tile map.
- Persist sessions so they survive process death.

---

## Architecture

```
UI (Compose)
  └─ SessionViewModel          # UI state owner; survives rotation
       └─ TrackingRepository   # single source of truth for domain state
            ├─ LocationSource  # abstraction over FusedLocationProvider
            └─ SessionStore    # abstraction over persistence (Room)
```

**Rules:**
- The ViewModel holds *only* UI state derived from the repository. No business logic.
- The Repository owns all business rules (e.g. "don't record points while paused").
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
    val startedAt: Long? = null,   // null until start() is called
    val stoppedAt: Long? = null,   // null until stop() is called
    val state: TrackingState = TrackingState.IDLE,
    val points: List<TrackPoint> = emptyList(),
)
```

**Why enums for state:** transitions can be validated in one place; no scattered boolean flags.

**Why nullable timestamps:** `null` unambiguously means "not yet reached that state", with no magic sentinel values.

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

Enforce in the repository:

```kotlin
fun TrackingSession.withState(next: TrackingState): TrackingSession {
    val allowed = mapOf(
        IDLE     to setOf(TRACKING),
        TRACKING to setOf(PAUSED, STOPPED),
        PAUSED   to setOf(TRACKING, STOPPED),
        STOPPED  to emptySet(),
    )
    require(next in allowed.getValue(state)) {
        "Invalid transition: $state → $next"
    }
    return copy(state = next)
}
```

This is the **only** place transition legality is checked.

---

## Key Interfaces

```kotlin
/** Abstracts location hardware. */
interface LocationSource {
    /** Emits fixes only while collected; caller controls lifecycle. */
    val locations: Flow<TrackPoint>
}

/** Abstracts persistence. */
interface SessionStore {
    suspend fun save(session: TrackingSession): TrackingSession   // returns with assigned id
    suspend fun load(id: Long): TrackingSession?
    fun observeAll(): Flow<List<TrackingSession>>
}
```

---

## TrackingRepository

```kotlin
class TrackingRepository(
    private val store: SessionStore,
    private val locationSource: LocationSource,
    private val scope: CoroutineScope,
) {
    private val _session = MutableStateFlow<TrackingSession?>(null)
    val session: StateFlow<TrackingSession?> = _session.asStateFlow()

    private var locationJob: Job? = null

    suspend fun createSession() {
        _session.value = store.save(TrackingSession())
    }

    suspend fun start() = transition(TRACKING) {
        _session.update { it?.copy(startedAt = System.currentTimeMillis()) }
        startCollecting()
    }

    suspend fun pause()  = transition(PAUSED)   { stopCollecting() }
    suspend fun resume() = transition(TRACKING) { startCollecting() }

    suspend fun stop() = transition(STOPPED) {
        _session.update { it?.copy(stoppedAt = System.currentTimeMillis()) }
        stopCollecting()
    }

    private suspend fun transition(
        next: TrackingState,
        sideEffect: suspend () -> Unit = {},
    ) {
        val updated = requireSession().withState(next)
        sideEffect()
        _session.value = store.save(updated)
    }

    private fun startCollecting() {
        locationJob = scope.launch {
            locationSource.locations.collect { point ->
                _session.update { it?.copy(points = it.points + point) }
                // persist incrementally so no points are lost on crash
                _session.value?.let { store.save(it) }
            }
        }
    }

    private fun stopCollecting() { locationJob?.cancel(); locationJob = null }

    private fun requireSession() =
        _session.value ?: error("No active session")
}
```

---

## ViewModel

```kotlin
data class SessionUiState(
    val startedAt: Long? = null,
    val stoppedAt: Long? = null,
    val trackingState: TrackingState = TrackingState.IDLE,
    val points: List<TrackPoint> = emptyList(),
    val error: String? = null,
)

class SessionViewModel(private val repo: TrackingRepository) : ViewModel() {

    val uiState: StateFlow<SessionUiState> = repo.session
        .map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionUiState())

    fun createSession() = launch { repo.createSession() }
    fun start()  = launch { repo.start() }
    fun pause()  = launch { repo.pause() }
    fun resume() = launch { repo.resume() }
    fun stop()   = launch { repo.stop() }

    private fun launch(block: suspend () -> Unit) =
        viewModelScope.launch { runCatching(block).onFailure { /* expose via uiState.error */ } }
}

private fun TrackingSession?.toUiState() = if (this == null) SessionUiState() else
    SessionUiState(startedAt = startedAt, stoppedAt = stoppedAt, trackingState = state, points = points)
```

---

## Persistence (Room)

```kotlin
@Entity data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long?,        // null until start() is called
    val stoppedAt: Long?,        // null until stop() is called
    val state: String,           // TrackingState.name()
    val pointsJson: String,      // JSON array of TrackPoints
)
```

- `RoomSessionStore : SessionStore` — the only production implementation.
- Serialize `List<TrackPoint>` to/from JSON with a single `TypeConverter` using `kotlinx.serialization`.
- **No duplication:** the converter lives in one file and is registered once.

---

## UI (Jetpack Compose)

### Screen structure

```
RouteTrackerScreen
  ├─ if session == null → CreateSessionCard   // single "New Session" button, no text input
  └─ else
       ├─ SessionHeader(startedAt, stoppedAt) // formatted timestamps
       ├─ OsmMapView(points)                  // shows route polyline
       └─ TrackingControlsBar(state)          // context-sensitive buttons
```

### Timestamp formatting (single location)

Format timestamps at the UI boundary only — never in the ViewModel or repository:

```kotlin
// SessionHeader.kt
private val timestampFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

fun Long?.toDisplayTime(): String =
    this?.let { timestampFormatter.format(Date(it)) } ?: "—"
```

Use `startedAt.toDisplayTime()` and `stoppedAt.toDisplayTime()` wherever labels are needed. This is the **only** place formatting is defined.

### `TrackingControlsBar` — button visibility rules (single source of truth)

```kotlin
data class ControlsState(
    val showStart: Boolean,
    val showPause: Boolean,
    val showResume: Boolean,
    val showStop: Boolean,
)

fun TrackingState.toControlsState() = ControlsState(
    showStart  = this == IDLE,
    showPause  = this == TRACKING,
    showResume = this == PAUSED,
    showStop   = this == TRACKING || this == PAUSED,
)
```

Compute once from `TrackingState`; pass the result to the composable. Never derive button visibility inside the composable.

### OSM Map

Use **osmdroid** (`org.osmdroid:osmdroid-android`).

```kotlin
@Composable
fun OsmMapView(points: List<TrackPoint>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
            }
        },
        update = { map ->
            map.overlays.removeAll { it is Polyline }
            if (points.isNotEmpty()) {
                val line = Polyline().apply {
                    setPoints(points.map { GeoPoint(it.lat, it.lon) })
                }
                map.overlays.add(line)
                map.controller.setCenter(GeoPoint(points.last().lat, points.last().lon))
                map.controller.setZoom(16.0)
            }
            map.invalidate()
        },
        modifier = modifier,
    )
}
```

---

## Permissions

Declare in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />              <!-- OSM tiles -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> <!-- osmdroid -->
```

Request `ACCESS_FINE_LOCATION` at runtime (use `rememberPermissionState` from Accompanist or the Compose `rememberLauncherForActivityResult` API) before calling `repo.start()`.

---

## Foreground Service

Tracking must survive the app going to the background.

```kotlin
class TrackingService : Service() {
    // Bind to repo via Hilt or manual DI
    // Post an ongoing notification while state == TRACKING or PAUSED
    // Stop self when state == STOPPED
}
```

- Start with `startForegroundService()` in `start()`; stop in `stop()`.
- The service does **not** own business logic — it only keeps the process alive.

---

## Dependency Injection (Hilt)

```
@HiltAndroidApp Application
@AndroidEntryPoint Activity / Service
@HiltViewModel SessionViewModel

AppModule provides:
  - LocationSource  → FusedLocationSource (production)
  - SessionStore    → RoomSessionStore (production)
  - CoroutineScope  → applicationScope
```

Tests replace `LocationSource` and `SessionStore` with fakes — no Hilt needed in unit tests.

---

## Testing Strategy

### Unit tests (JVM, no Android runtime)

| Subject | What to test |
|---------|-------------|
| `TrackingSession.withState()` | All valid transitions succeed; all invalid ones throw. |
| `TrackingRepository` | Points are only added while TRACKING; `save()` is called after each point; transitions update `session` state flow; `startedAt` is set (non-null) after `start()` and unchanged after `pause()`/`resume()`; `stoppedAt` is null before `stop()` and non-null after. |
| `TrackingState.toControlsState()` | Correct button visibility for every state. |
| `TrackingSession?.toUiState()` | Null and non-null mappings, including timestamp fields. |
| `Long?.toDisplayTime()` | Non-null value formats correctly; null returns `"—"`. |

Use **fakes, not mocks**:

```kotlin
class FakeLocationSource(private val points: List<TrackPoint>) : LocationSource {
    override val locations = points.asFlow()
}

class FakeSessionStore : SessionStore {
    private var nextId = 1L
    private val db = mutableMapOf<Long, TrackingSession>()
    override suspend fun save(s: TrackingSession) =
        s.copy(id = if (s.id == 0L) nextId++ else s.id).also { db[it.id] = it }
    override suspend fun load(id: Long) = db[id]
    override fun observeAll() = flowOf(db.values.toList())
}
```

### Instrumented / UI tests

- Verify the map composable renders without crashing (smoke test).
- Verify button visibility changes when `TrackingState` changes using `ComposeTestRule`.
- Verify `SessionHeader` displays `"—"` before start and a formatted string after.

---

## Dependencies (gradle)

```kotlin
// Core
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8+")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8+")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6+")

// Map
implementation("org.osmdroid:osmdroid-android:6.1+")

// Persistence
implementation("androidx.room:room-runtime:2.6+")
implementation("androidx.room:room-ktx:2.6+")
ksp("androidx.room:room-compiler:2.6+")

// DI
implementation("com.google.dagger:hilt-android:2.51+")
ksp("com.google.dagger:hilt-compiler:2.51+")

// Location
implementation("com.google.android.gms:play-services-location:21+")

// Testing
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8+")
testImplementation("junit:junit:4.13+")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
```

---

## File Layout

```
app/src/
  main/
    kotlin/com/example/routetracker/
      domain/
        TrackPoint.kt
        TrackingSession.kt          // + withState() extension
        TrackingState.kt            // + toControlsState() extension
      data/
        LocationSource.kt           // interface + FusedLocationSource
        SessionStore.kt             // interface
        room/
          SessionEntity.kt
          SessionDao.kt
          TrackPointConverter.kt
          RoomSessionStore.kt
          AppDatabase.kt
      repository/
        TrackingRepository.kt
      ui/
        SessionViewModel.kt         // + SessionUiState + toUiState()
        RouteTrackerScreen.kt
        SessionHeader.kt            // + toDisplayTime() extension
        OsmMapView.kt
        TrackingControlsBar.kt      // + ControlsState
      service/
        TrackingService.kt
      di/
        AppModule.kt
  test/
    kotlin/com/example/routetracker/
      domain/TrackingSessionTest.kt
      domain/TrackingStateTest.kt
      repository/TrackingRepositoryTest.kt
      ui/SessionViewModelTest.kt
      ui/SessionHeaderTest.kt
      fakes/
        FakeLocationSource.kt
        FakeSessionStore.kt
```

---

## Implementation Checklist

- [ ] Domain model + `withState()` + unit tests
- [ ] `toControlsState()` + unit tests
- [ ] `FakeLocationSource` and `FakeSessionStore`
- [ ] `TrackingRepository` + unit tests (using fakes), including timestamp assertions
- [ ] Room entities, DAO, converter, `RoomSessionStore`
- [ ] `FusedLocationSource`
- [ ] Hilt module wiring
- [ ] `SessionViewModel` + `toUiState()` + unit tests
- [ ] `toDisplayTime()` extension + unit tests
- [ ] `OsmMapView` composable
- [ ] `TrackingControlsBar` composable
- [ ] `SessionHeader` composable
- [ ] `RouteTrackerScreen` composable (permission request included)
- [ ] `TrackingService` (foreground)
- [ ] Manifest permissions + service declaration
- [ ] Smoke UI tests
