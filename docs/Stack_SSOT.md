# Stack SSOT — Technical Specification v4.0

> **Single Source of Truth (SSOT)**
> All implementation prompts, code, and architectural decisions must conform to this document.
> Conflicts between any other source and this SSOT are resolved in favor of this SSOT.

---

## Table of Contents

1. [Document Purpose & Product Overview](#1-document-purpose--product-overview)
2. [Hard Constraints (Absolute Rules)](#2-hard-constraints-absolute-rules)
3. [Technology Stack & Library Versions](#3-technology-stack--library-versions)
4. [Module Architecture](#4-module-architecture)
5. [Gate (Onboarding) System](#5-gate-onboarding-system)
6. [Storage Access & Folder Browser](#6-storage-access--folder-browser)
7. [Scan & Sync Policy](#7-scan--sync-policy)
8. [Data Models & Database Schema](#8-data-models--database-schema)
9. [Playback Engine](#9-playback-engine)
10. [Tag System](#10-tag-system)
11. [Playlist System](#11-playlist-system)
12. [Lyrics System](#12-lyrics-system)
13. [Search (FTS5)](#13-search-fts5)
14. [Equalizer](#14-equalizer)
15. [A-B Repeat](#15-a-b-repeat)
16. [Notification & Foreground Service](#16-notification--foreground-service)
17. [File Deletion Flow](#17-file-deletion-flow)
18. [Backup & Restore](#18-backup--restore)
19. [UI / UX Specification](#19-ui--ux-specification)
20. [Navigation & Deep Links](#20-navigation--deep-links)
21. [Settings](#21-settings)
22. [State Management & Architecture Patterns](#22-state-management--architecture-patterns)
23. [Diagnostics & Stability](#23-diagnostics--stability)
24. [Internationalization (i18n)](#24-internationalization-i18n)
25. [Non-Functional Requirements](#25-non-functional-requirements)
26. [Acceptance Criteria](#26-acceptance-criteria)
27. [Development Phases](#27-development-phases)
28. [v2.0+ Roadmap](#28-v20-roadmap)

---

## 1. Document Purpose & Product Overview

### 1.1 Purpose

This document is the **Single Source of Truth** for Stack, an Android local music player application. Every Claude Code implementation session must read this document first and derive all architectural, behavioral, and UI decisions from it.

### 1.2 Product Concept

**"Notion + Soft Archiving"** — A local music archive app that reorganizes folder/file-based libraries through tags, playlists, and powerful search, with a clean, typography-driven, Notion-style aesthetic.

**Core Differentiators:**
- Tag-centric organization (system tags + custom tags)
- Stable playback engine with race-condition-proof architecture
- Full lyrics support with synced scrolling and user editing
- Adaptive UI for phones, tablets, and foldables

### 1.3 Package & Identity

| Item | Value |
|------|-------|
| Package name | `com.stack.player` |
| App name | Stack |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |
| Compile SDK | 35 |
| JDK | 17 |
| Kotlin | 2.0.21 |

### 1.4 SSOT Principles

1. **Library versions are fixed once** and only updated at PR boundaries.
2. Mid-development upgrades or experimental branch merges are **strictly prohibited**.
3. All dependency versions are centrally managed in `libs.versions.toml`.
4. Every implementation prompt must derive from this document.
5. Conflicts between prompts and this SSOT are resolved in favor of **this SSOT**.

---

## 2. Hard Constraints (Absolute Rules)

These constraints are **inviolable** across all implementation phases. Any code that violates them is immediately rejected.

### 2.1 GateReady Rule

```
GateReady = (Required Permissions OK) AND (SAF Source Folders ≥ 1)
```

**Before GateReady (Not Ready) — ALL PROHIBITED:**
- File scan initiation or scheduling
- Session restore or auto-play operations
- MiniPlayer / NowPlaying UI display
- Library content display (only empty state / onboarding shown)
- Queue construction or playback state manipulation
- Search functionality

**At GateReady Moment (first transition to Ready) — exactly once:**
1. Session restore executes, but **ALWAYS in Pause state** (never auto-play).
2. Initial full scan triggers once; subsequent scans follow incremental policy.
3. One-time snackbar: "Session restored in paused state" (no exaggeration).

**Implementation:** `GateReadyState` is the single SSOT, backed by DataStore + Flow. State transitions detected on: app start, permission change, folder change.

### 2.2 Architecture Dependency Rules

| Layer | May Depend On | Must NOT Depend On |
|-------|---------------|-------------------|
| `:core` | Nothing | Everything |
| `:domain` | `:core` only | `:data`, `:feature:*`, `:app` |
| `:data` | `:domain`, `:core` | `:feature:*`, `:app` |
| `:core:player` | `:domain`, `:core` | `:data`, `:feature:*`, `:app` |
| `:feature:*` | `:domain`, `:core` | `:data` directly, other `:feature:*` |
| `:app` | All modules (DI assembly) | N/A (top-level) |

### 2.3 Playback Race Condition Prevention

All player state mutations **must** be serialized. No concurrent state modifications allowed.

- Main-thread state lock (`withStateLockMain`) + snapshot pattern: commands execute against a frozen snapshot of current state.
- Player callbacks (track end, error) and UI commands (AddNext, Skip, Seek) must never conflict.
- Crossfade transitions interrupted by user commands: lock serializes, snapshot-based commit, stale transitions discarded.
- A-B repeat loop-back is also routed through CommandDispatcher.

### 2.4 No Silent Failures

When feature combinations conflict (e.g., Crossfade ON forces Gapless OFF):
1. Auto-disable the conflicting feature.
2. Display the reason to the user via Snackbar or inline message.

Silent failures are **prohibited**.

### 2.5 No Hardcoded User-Facing Strings

All user-facing strings must use Android string resources (`R.string.*`) for i18n support. Zero hardcoded strings in Composables or ViewModels.

---

## 3. Technology Stack & Library Versions

### 3.1 Core Dependencies

All versions are declared in `gradle/libs.versions.toml`.

| Library | Version | Notes |
|---------|---------|-------|
| Kotlin | 2.0.21 | K2 compiler, stable |
| KSP | 2.0.21-1.0.28 | Must match Kotlin version exactly |
| AGP | 8.7.3 | Latest stable, targetSdk 35 |
| Compose BOM | 2024.12.01 | Material 3 full support |
| Navigation Compose | 2.8.5 | Type-safe navigation |
| Hilt | 2.56.2 | KSP full support |
| Hilt Navigation Compose | 1.2.0 | ViewModel injection in NavHost |
| Room | 2.6.1 | KSP, Android-only optimized |
| DataStore Preferences | 1.1.3 | Settings persistence |
| Media3 (ExoPlayer) | 1.5.1 | Stable, critical bug fixes |
| Coil | 3.0.4 | Compose-native image loading |
| Coroutines | 1.9.0 | Stable |
| Lifecycle | 2.8.7 | Compose integration |
| Activity Compose | 1.9.3 | WindowSizeClass support |
| Core KTX | 1.15.0 | Kotlin extensions |
| AppCompat | 1.7.0 | Theme compatibility |

### 3.2 Testing Dependencies

| Library | Version | Notes |
|---------|---------|-------|
| JUnit | 4.13.2 | Unit testing |
| AndroidX Test JUnit | 1.2.1 | Instrumented tests |
| Espresso Core | 3.6.1 | UI testing |
| Mockk | 1.13.x | Kotlin mocking |
| Turbine | 1.1.x | Flow testing |
| Coroutines Test | 1.9.0 | Coroutine testing |

### 3.3 Compatibility Matrix

All of the following combinations have been verified as compatible:

- Kotlin 2.0.21 + KSP 2.0.21-1.0.28 ✓
- Kotlin 2.0.21 + Hilt 2.56.2 (KSP) ✓
- Kotlin 2.0.21 + Room 2.6.1 (KSP) ✓
- Compose BOM 2024.12.01 + Navigation 2.8.5 ✓
- AGP 8.7.3 + targetSdk 35 ✓
- Media3 1.5.1 + targetSdk 35 ✓

---

## 4. Module Architecture

### 4.1 Module List (13 modules)

```
stack/
├── app/                    # Application entry, DI assembly, NavHost
├── core/                   # Common utilities, Result, Theme, UI components
│   └── player/             # Playback engine (no UI dependencies)
├── data/                   # Room, DataStore, Scanner, Repository implementations
├── domain/                 # UseCases, Repository interfaces, Domain models
└── feature/
    ├── gate/               # Onboarding flow
    ├── library/            # Library 4-tab browsing + detail screens
    ├── player/             # NowPlaying UI, MiniPlayer, MediaService
    ├── search/             # FTS5 search
    ├── tags/               # Tag management UI
    ├── playlists/          # Playlist management UI
    ├── settings/           # Settings + Equalizer + Backup/Restore
    └── crashlog/           # In-app crash viewer
```

### 4.2 Dependency Graph

```
:app ──→ :feature:* ──→ :domain ──→ :core
              │                        ↑
              │         :data ─────────┘
              │           ↑
              │      :domain, :core
              │
              └──→ :core:player ──→ :domain, :core
```

### 4.3 Module Responsibilities

#### :app
- `StackApplication.kt` — `@HiltAndroidApp` entry point
- `MainActivity.kt` — Single Activity, Compose host, WindowSizeClass detection
- `navigation/StackNavHost.kt` — Central NavHost with all routes
- `navigation/NavRoutes.kt` — Route constants, type-safe arguments
- `di/AppModule.kt` — Top-level Hilt module binding

#### :core
- `util/CoroutineDispatchers.kt` — Injectable dispatcher provider
- `util/Result.kt` — Sealed `Result<T>` type (Success / Error / Loading)
- `util/DateTimeUtil.kt` — Timestamp formatting
- `util/DurationUtil.kt` — Duration formatting (mm:ss, hh:mm:ss)
- `util/StringUtil.kt` — String manipulation utilities
- `util/FileUtil.kt` — File extension/name utilities
- `util/FlowExtensions.kt` — Flow throttle, debounce extensions
- `logging/Logger.kt` — Structured logging wrapper (Timber-based)
- `logging/LoggerModule.kt` — Hilt module for Logger
- `crash/CrashCapture.kt` — Crash capture interface + local storage implementation
- `ui/theme/Theme.kt` — StackTheme composable (Material3, Dynamic Color support)
- `ui/theme/Color.kt` — Color palette definitions
- `ui/theme/Typography.kt` — Typography scale (Notion-like)
- `ui/theme/Spacing.kt` — Spacing tokens object
- `ui/theme/Shape.kt` — Shape definitions
- `ui/components/LoadingState.kt` — Reusable loading indicator
- `ui/components/ErrorState.kt` — Reusable error display with retry action
- `ui/components/EmptyState.kt` — Reusable empty state with icon + message + action
- `ui/components/ArtworkImage.kt` — Coil-based album art component with placeholder

#### :core:player
- `StackPlayerManager.kt` — Dual ExoPlayer coordinator (play, pause, seek, skip, crossfade)
- `PlaybackQueue.kt` — Queue management (original + shuffled lists, AddNext, reorder)
- `PlaybackStateReducer.kt` — Single source of truth: `PlaybackState` (StateFlow)
- `PlaybackCommand.kt` — Sealed interface for all playback commands
- `CommandDispatcher.kt` — Mutex-based command serialization with snapshot pattern
- `AudioFocusHandler.kt` — Audio focus gain/loss/duck management
- `CrossfadeController.kt` — Crossfade transition logic (3-branch: overlap/gapless/gap)
- `EqualizerManager.kt` — Android AudioEffect EQ session management
- `ABRepeatController.kt` — A-B point tracking and auto-seek logic
- `PlayerModule.kt` — Hilt module for player dependencies

#### :data
- `local/db/StackDatabase.kt` — Room database definition (13 tables)
- `local/db/entity/` — All entity classes
- `local/db/dao/` — All DAO interfaces
- `local/db/converter/TypeConverters.kt` — Room type converters
- `local/preferences/PreferencesDataStore.kt` — DataStore wrapper
- `local/preferences/PreferencesKeys.kt` — All DataStore key constants
- `repository/` — All Repository interface implementations
- `scanner/MediaStoreScanner.kt` — MediaStore query + metadata extraction
- `scanner/ScanManager.kt` — Scan orchestration with Mutex
- `scanner/ContentObserverManager.kt` — MediaStore change detection + debounce
- `scanner/LrcFileScanner.kt` — .lrc file discovery and parsing
- `mapper/` — Entity ↔ Domain model mappers
- `backup/BackupManager.kt` — Export logic
- `backup/RestoreManager.kt` — Import + merge logic
- `di/DatabaseModule.kt` — Hilt module: Database, DAOs
- `di/RepositoryModule.kt` — Hilt module: Repository bindings
- `di/ScannerModule.kt` — Hilt module: Scanner bindings

#### :domain
- `model/` — All domain models (Track, Tag, Playlist, Lyrics, etc.)
- `model/enums/` — TrackStatus, SystemTagType, SortOrder, RepeatMode, ShuffleMode, CrossfadePolicy, LyricsSyncType
- `repository/` — All repository interfaces
- `usecase/playback/` — PlayTrackUseCase, AddNextUseCase, ToggleShuffleUseCase, etc.
- `usecase/library/` — GetTracksUseCase, GetAlbumDetailUseCase, GetArtistDetailUseCase, etc.
- `usecase/scan/` — ScanLibraryUseCase, RescanUseCase
- `usecase/tag/` — CreateTagUseCase, ToggleFavoriteUseCase, AssignTagUseCase, etc.
- `usecase/playlist/` — CreatePlaylistUseCase, AddToPlaylistUseCase, ReorderPlaylistUseCase, etc.
- `usecase/lyrics/` — GetLyricsUseCase, SaveLyricsUseCase, ImportLrcUseCase, etc.
- `usecase/search/` — SearchTracksUseCase, GetRecentSearchesUseCase
- `usecase/backup/` — CreateBackupUseCase, RestoreBackupUseCase
- `usecase/settings/` — GetSettingsUseCase, UpdateSettingUseCase

#### :feature:gate
- `GateScreen.kt` — Permission → Folder → Scan card flow
- `GateViewModel.kt` — MVI state machine
- `PermissionManager.kt` — API-level-aware permission handling

#### :feature:library
- `LibraryShellScreen.kt` — Scaffold + BottomNav + MiniPlayer host
- `tracks/TracksTab.kt` — Track list with sort/filter
- `albums/AlbumsTab.kt` — Album grid
- `albums/AlbumDetailScreen.kt` — Album detail (art + track list)
- `artists/ArtistsTab.kt` — Artist list
- `artists/ArtistDetailScreen.kt` — Artist detail (albums + tracks)
- `folders/FoldersTab.kt` — Folder breadcrumb browser
- `viewmodel/LibraryViewModel.kt` — Shared library state
- `viewmodel/AlbumDetailViewModel.kt`
- `viewmodel/ArtistDetailViewModel.kt`

#### :feature:player
- `NowPlayingScreen.kt` — Full-screen player
- `MiniPlayer.kt` — Compact bar player
- `QueueSheet.kt` — BottomSheet with drag-reorder + swipe-dismiss
- `LyricsOverlay.kt` — Synced lyrics scroll overlay
- `ABRepeatControls.kt` — A/B point markers on seekbar
- `PlayerViewModel.kt` — Bridges :core:player state to UI
- `service/StackMediaService.kt` — MediaSessionService implementation

#### :feature:search
- `SearchScreen.kt` — Search bar + results + recent searches
- `SearchViewModel.kt` — Debounced FTS query logic

#### :feature:tags
- `TagsScreen.kt` — Tag list/grid management
- `TagDetailScreen.kt` — Tracks associated with a tag
- `TagEditorDialog.kt` — Create/edit tag (name + color picker)
- `TagsViewModel.kt`

#### :feature:playlists
- `PlaylistsScreen.kt` — Playlist list
- `PlaylistDetailScreen.kt` — Playlist tracks with reorder
- `PlaylistEditorDialog.kt` — Create/edit playlist metadata
- `PlaylistsViewModel.kt`

#### :feature:settings
- `SettingsScreen.kt` — Main settings list
- `EqualizerScreen.kt` — EQ bands + presets
- `ScanFolderScreen.kt` — SAF folder management
- `BackupRestoreScreen.kt` — Backup/restore execution
- `LyricsEditorScreen.kt` — LRC file assign/edit
- `SettingsViewModel.kt`

#### :feature:crashlog
- `CrashLogScreen.kt` — Crash list + detail
- `CrashLogViewModel.kt`

### 4.4 Detailed File Tree

```
stack/
├── gradle/
│   └── libs.versions.toml              # Single version catalog (SSOT for deps)
├── build.gradle.kts                     # Root build script
├── settings.gradle.kts                  # Module includes
│
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/stack/
│       │   ├── StackApplication.kt
│       │   ├── MainActivity.kt
│       │   ├── navigation/
│       │   │   ├── StackNavHost.kt
│       │   │   └── NavRoutes.kt
│       │   └── di/
│       │       └── AppModule.kt
│       └── res/
│           ├── values/strings.xml           # English (default)
│           ├── values-ko/strings.xml        # Korean
│           └── values-ja/strings.xml        # Japanese
│
├── core/
│   ├── build.gradle.kts
│   ├── src/main/kotlin/com/stack/core/
│   │   ├── di/
│   │   │   └── DispatcherModule.kt
│   │   ├── logging/
│   │   │   ├── Logger.kt
│   │   │   └── LoggerModule.kt
│   │   ├── crash/
│   │   │   └── CrashCapture.kt
│   │   ├── util/
│   │   │   ├── CoroutineDispatchers.kt
│   │   │   ├── Result.kt
│   │   │   ├── DateTimeUtil.kt
│   │   │   ├── DurationUtil.kt
│   │   │   ├── StringUtil.kt
│   │   │   ├── FileUtil.kt
│   │   │   └── FlowExtensions.kt
│   │   └── ui/
│   │       ├── theme/
│   │       │   ├── Theme.kt
│   │       │   ├── Color.kt
│   │       │   ├── Typography.kt
│   │       │   ├── Spacing.kt
│   │       │   └── Shape.kt
│   │       └── components/
│   │           ├── LoadingState.kt
│   │           ├── ErrorState.kt
│   │           ├── EmptyState.kt
│   │           └── ArtworkImage.kt
│   │
│   └── player/
│       ├── build.gradle.kts
│       └── src/main/kotlin/com/stack/core/player/
│           ├── StackPlayerManager.kt
│           ├── PlaybackQueue.kt
│           ├── PlaybackStateReducer.kt
│           ├── PlaybackCommand.kt
│           ├── CommandDispatcher.kt
│           ├── AudioFocusHandler.kt
│           ├── CrossfadeController.kt
│           ├── EqualizerManager.kt
│           ├── ABRepeatController.kt
│           └── di/
│               └── PlayerModule.kt
│
├── data/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/stack/data/
│       ├── local/
│       │   ├── db/
│       │   │   ├── StackDatabase.kt
│       │   │   ├── converter/
│       │   │   │   └── TypeConverters.kt
│       │   │   ├── entity/
│       │   │   │   ├── TrackEntity.kt
│       │   │   │   ├── TrackFtsEntity.kt
│       │   │   │   ├── AlbumEntity.kt
│       │   │   │   ├── ArtistEntity.kt
│       │   │   │   ├── TagEntity.kt
│       │   │   │   ├── TrackTagCrossRef.kt
│       │   │   │   ├── PlaylistEntity.kt
│       │   │   │   ├── PlaylistTrackCrossRef.kt
│       │   │   │   ├── LyricsEntity.kt
│       │   │   │   ├── PlayHistoryEntity.kt
│       │   │   │   ├── SourceFolderEntity.kt
│       │   │   │   ├── PlaybackSessionEntity.kt
│       │   │   │   └── CrashReportEntity.kt
│       │   │   └── dao/
│       │   │       ├── TrackDao.kt
│       │   │       ├── TrackFtsDao.kt
│       │   │       ├── AlbumDao.kt
│       │   │       ├── ArtistDao.kt
│       │   │       ├── TagDao.kt
│       │   │       ├── PlaylistDao.kt
│       │   │       ├── LyricsDao.kt
│       │   │       ├── PlayHistoryDao.kt
│       │   │       ├── SourceFolderDao.kt
│       │   │       ├── PlaybackSessionDao.kt
│       │   │       └── CrashReportDao.kt
│       │   └── preferences/
│       │       ├── PreferencesDataStore.kt
│       │       └── PreferencesKeys.kt
│       ├── repository/
│       │   ├── TrackRepositoryImpl.kt
│       │   ├── AlbumRepositoryImpl.kt
│       │   ├── ArtistRepositoryImpl.kt
│       │   ├── TagRepositoryImpl.kt
│       │   ├── PlaylistRepositoryImpl.kt
│       │   ├── LyricsRepositoryImpl.kt
│       │   ├── PlayHistoryRepositoryImpl.kt
│       │   ├── SourceFolderRepositoryImpl.kt
│       │   ├── SettingsRepositoryImpl.kt
│       │   ├── PlaybackSessionRepositoryImpl.kt
│       │   └── CrashReportRepositoryImpl.kt
│       ├── scanner/
│       │   ├── MediaStoreScanner.kt
│       │   ├── ScanManager.kt
│       │   ├── ContentObserverManager.kt
│       │   └── LrcFileScanner.kt
│       ├── backup/
│       │   ├── BackupManager.kt
│       │   └── RestoreManager.kt
│       ├── mapper/
│       │   ├── TrackMapper.kt
│       │   ├── AlbumMapper.kt
│       │   ├── ArtistMapper.kt
│       │   ├── TagMapper.kt
│       │   ├── PlaylistMapper.kt
│       │   └── LyricsMapper.kt
│       └── di/
│           ├── DatabaseModule.kt
│           ├── RepositoryModule.kt
│           └── ScannerModule.kt
│
├── domain/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/stack/domain/
│       ├── model/
│       │   ├── Track.kt
│       │   ├── Album.kt
│       │   ├── Artist.kt
│       │   ├── Tag.kt
│       │   ├── Playlist.kt
│       │   ├── PlaylistTrack.kt
│       │   ├── Lyrics.kt
│       │   ├── LyricsLine.kt
│       │   ├── PlayHistory.kt
│       │   ├── SourceFolder.kt
│       │   ├── PlaybackSession.kt
│       │   ├── CrashReport.kt
│       │   ├── SearchResult.kt
│       │   ├── ScanProgress.kt
│       │   ├── BackupMetadata.kt
│       │   └── enums/
│       │       ├── TrackStatus.kt
│       │       ├── SystemTagType.kt
│       │       ├── SortOrder.kt
│       │       ├── RepeatMode.kt
│       │       ├── ShuffleMode.kt
│       │       ├── CrossfadePolicy.kt
│       │       └── LyricsSyncType.kt
│       ├── repository/
│       │   ├── TrackRepository.kt
│       │   ├── AlbumRepository.kt
│       │   ├── ArtistRepository.kt
│       │   ├── TagRepository.kt
│       │   ├── PlaylistRepository.kt
│       │   ├── LyricsRepository.kt
│       │   ├── PlayHistoryRepository.kt
│       │   ├── SourceFolderRepository.kt
│       │   ├── SettingsRepository.kt
│       │   ├── PlaybackSessionRepository.kt
│       │   └── CrashReportRepository.kt
│       └── usecase/
│           ├── playback/
│           │   ├── PlayTrackUseCase.kt
│           │   ├── PlayAlbumUseCase.kt
│           │   ├── PlayPlaylistUseCase.kt
│           │   ├── AddNextUseCase.kt
│           │   ├── ToggleShuffleUseCase.kt
│           │   ├── CycleRepeatModeUseCase.kt
│           │   ├── RestoreSessionUseCase.kt
│           │   └── SaveSessionUseCase.kt
│           ├── library/
│           │   ├── GetTracksUseCase.kt
│           │   ├── GetAlbumsUseCase.kt
│           │   ├── GetArtistsUseCase.kt
│           │   ├── GetAlbumDetailUseCase.kt
│           │   ├── GetArtistDetailUseCase.kt
│           │   ├── GetFolderContentsUseCase.kt
│           │   └── DeleteTrackUseCase.kt
│           ├── scan/
│           │   ├── ScanLibraryUseCase.kt
│           │   ├── RescanLibraryUseCase.kt
│           │   └── CleanGhostTracksUseCase.kt
│           ├── tag/
│           │   ├── GetTagsUseCase.kt
│           │   ├── CreateTagUseCase.kt
│           │   ├── UpdateTagUseCase.kt
│           │   ├── DeleteTagUseCase.kt
│           │   ├── ToggleFavoriteUseCase.kt
│           │   ├── AssignTagToTrackUseCase.kt
│           │   ├── RemoveTagFromTrackUseCase.kt
│           │   └── RefreshSystemTagsUseCase.kt
│           ├── playlist/
│           │   ├── GetPlaylistsUseCase.kt
│           │   ├── GetPlaylistDetailUseCase.kt
│           │   ├── CreatePlaylistUseCase.kt
│           │   ├── UpdatePlaylistUseCase.kt
│           │   ├── DeletePlaylistUseCase.kt
│           │   ├── AddTrackToPlaylistUseCase.kt
│           │   ├── RemoveTrackFromPlaylistUseCase.kt
│           │   └── ReorderPlaylistUseCase.kt
│           ├── lyrics/
│           │   ├── GetLyricsUseCase.kt
│           │   ├── SaveLyricsUseCase.kt
│           │   ├── ImportLrcFileUseCase.kt
│           │   └── ExportLrcFileUseCase.kt
│           ├── search/
│           │   ├── SearchUseCase.kt
│           │   ├── GetRecentSearchesUseCase.kt
│           │   └── ClearRecentSearchesUseCase.kt
│           ├── backup/
│           │   ├── CreateBackupUseCase.kt
│           │   └── RestoreBackupUseCase.kt
│           └── settings/
│               ├── GetSettingsUseCase.kt
│               └── UpdateSettingUseCase.kt
│
└── feature/
    ├── gate/
    │   ├── build.gradle.kts
    │   └── src/main/kotlin/com/stack/feature/gate/
    │       ├── GateScreen.kt
    │       ├── GateViewModel.kt
    │       └── PermissionManager.kt
    │
    ├── library/
    │   ├── build.gradle.kts
    │   └── src/main/kotlin/com/stack/feature/library/
    │       ├── LibraryShellScreen.kt
    │       ├── tracks/TracksTab.kt
    │       ├── albums/AlbumsTab.kt
    │       ├── albums/AlbumDetailScreen.kt
    │       ├── artists/ArtistsTab.kt
    │       ├── artists/ArtistDetailScreen.kt
    │       ├── folders/FoldersTab.kt
    │       └── viewmodel/
    │           ├── LibraryViewModel.kt
    │           ├── AlbumDetailViewModel.kt
    │           └── ArtistDetailViewModel.kt
    │
    ├── player/
    │   ├── build.gradle.kts
    │   └── src/main/kotlin/com/stack/feature/player/
    │       ├── NowPlayingScreen.kt
    │       ├── MiniPlayer.kt
    │       ├── QueueSheet.kt
    │       ├── LyricsOverlay.kt
    │       ├── ABRepeatControls.kt
    │       ├── PlayerViewModel.kt
    │       └── service/
    │           └── StackMediaService.kt
    │
    ├── search/
    │   ├── build.gradle.kts
    │   └── src/main/kotlin/com/stack/feature/search/
    │       ├── SearchScreen.kt
    │       └── SearchViewModel.kt
    │
    ├── tags/
    │   ├── build.gradle.kts
    │   └── src/main/kotlin/com/stack/feature/tags/
    │       ├── TagsScreen.kt
    │       ├── TagDetailScreen.kt
    │       ├── TagEditorDialog.kt
    │       └── TagsViewModel.kt
    │
    ├── playlists/
    │   ├── build.gradle.kts
    │   └── src/main/kotlin/com/stack/feature/playlists/
    │       ├── PlaylistsScreen.kt
    │       ├── PlaylistDetailScreen.kt
    │       ├── PlaylistEditorDialog.kt
    │       └── PlaylistsViewModel.kt
    │
    ├── settings/
    │   ├── build.gradle.kts
    │   └── src/main/kotlin/com/stack/feature/settings/
    │       ├── SettingsScreen.kt
    │       ├── EqualizerScreen.kt
    │       ├── ScanFolderScreen.kt
    │       ├── BackupRestoreScreen.kt
    │       ├── LyricsEditorScreen.kt
    │       └── SettingsViewModel.kt
    │
    └── crashlog/
        ├── build.gradle.kts
        └── src/main/kotlin/com/stack/feature/crashlog/
            ├── CrashLogScreen.kt
            └── CrashLogViewModel.kt
```

---

## 5. Gate (Onboarding) System

### 5.1 Permission Deck (Card Swipe Flow)

| Step | Content | Details |
|------|---------|---------|
| 1 | Welcome | Brand splash: "Record your music" |
| 2 | Media Access | Android 13+: `READ_MEDIA_AUDIO` / 12−: `READ_EXTERNAL_STORAGE` |
| 3 | Notification | Android 13+ only: `POST_NOTIFICATIONS` |
| 4 | Source Folder (SAF) | **REQUIRED**: ≥1 folder. `takePersistableUriPermission()` for reboot survival. |
| 5 | Initial Scan | Auto-starts. Progress bar (%) + scanned track count animation. |

### 5.2 GateReady State Machine

| Condition | Behavior |
|-----------|----------|
| Not Ready → Ready (first time) | Session restore (paused) + full scan + one-time snackbar |
| Ready → Not Ready (permission revoked) | Hide all library/player UI, show recovery prompt |
| Ready → Ready (folder added) | Trigger incremental scan for new folder |
| Ready → Ready (folder removed) | Mark affected tracks GHOST, show cleanup banner |

### 5.3 Failure/Denial UX

| Scenario | UI Response |
|----------|-------------|
| Permission denied | One-line explanation + single "Open Settings" button |
| No folder selected | "Select a folder containing music" + folder picker button |
| Scan failure | Error cause label + single "Retry" button |

**Rule:** Exactly one explanation line + exactly one action button. No multi-step error dialogs.

---

## 6. Storage Access & Folder Browser

### 6.1 Principles

1. Internal logic uses **SAF Tree URI + MediaStore contentUri** as canonical identifiers.
2. UI display paths are constructed relative to user-selected folder trees.
3. **STRICTLY PROHIBITED:** Hacking absolute paths via reflection or hidden APIs.

### 6.2 Path Display Policy

| Priority | Method | Example |
|----------|--------|---------|
| 1 (Preferred) | SAF tree relative path as Breadcrumb | `Storage / Music / Pop / 1990s` |
| 2 (Fallback) | MediaStore RELATIVE_PATH composition | `Internal Storage / Music / Pop` |
| 3 (Last resort) | Abbreviated with ellipsis | `Storage / … / <FolderName>` |

### 6.3 SAF Folder Management

- Users can register **multiple** SAF folders.
- Each folder's Tree URI is persisted with `takePersistableUriPermission()`.
- On app start, all persisted URIs are validated; stale permissions trigger user notification.
- Adding a folder → incremental scan for that subtree.
- Removing a folder → affected tracks marked GHOST, cleanup banner shown.

---

## 7. Scan & Sync Policy

### 7.1 Scan Triggers

| Trigger | Type | Details |
|---------|------|---------|
| GateReady first transition | Full scan | One-time mandatory scan |
| App launch | Incremental | Background on `Dispatchers.IO` |
| MediaStore change | Incremental | ContentObserver + 3-second debounce |
| Pull-to-refresh | Incremental | Immediate, user-initiated |
| Settings → Rescan | Full scan | Shows progress dialog, cancellable |

### 7.2 Scanner Operation

1. SAF treeUri recursive traversal for all registered folders.
2. Audio extension whitelist: `mp3`, `flac`, `m4a`, `ogg`, `opus`, `wav`, `aac`, `wma`, `aiff`.
3. Metadata extraction via `MediaMetadataRetriever`: title, artist, album, albumArtist, trackNo, discNo, genre, year, duration, bitrate, sampleRate.
4. Album art extraction: embedded artwork → `albumArtUri` stored as content URI.
5. LRC file discovery: for each audio file, check `<same_name>.lrc` in same directory.
6. **Extraction failure fallback:** Track created with minimum fields (fileName, size, contentUri).
7. Album/Artist normalization: distinct (albumName + albumArtist) pairs create Album entries; distinct artist names create Artist entries.

### 7.3 Track Identification & Deduplication

- **Primary key:** `contentUri` (unique identifier)
- **Auxiliary keys:** `size` + `dateModified` (change detection)
- Same contentUri = same track (always)
- Different contentUri but same (size + dateModified) = suspected duplicate → treated as separate tracks (false-positive prevention)

### 7.4 Ghost Policy (Deletion/Move Detection)

1. Previously indexed track missing from scan results → status set to `GHOST`.
2. Library shows "Cleanup needed" banner with "Remove all" / "Rescan" options.
3. Ghost tracks **excluded from**: search results, auto-play queue, queue construction.
4. Ghost → Active: if file reappears on next scan (e.g., SD card re-inserted).
5. User can toggle "Show ghost tracks" in Settings.

### 7.5 Performance & Stability

- All scans run on `Dispatchers.IO`, fully cancellable via `CoroutineScope`.
- Concurrent scan requests merge into **single pipeline** via Mutex (race prevention).
- UI subscribes to `ScanProgress` Flow with 500ms sampling to prevent recomposition storms.
- Minimum track duration filter: configurable in Settings (default: 0s).

---

## 8. Data Models & Database Schema

### 8.1 Room Database

Database name: `stack_database`
Version: 1
Export schema: `true` (to `data/schemas/`)

### 8.2 Entities (13 tables)

#### TrackEntity

```kotlin
@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val contentUri: String,       // MediaStore content URI (unique)
    val title: String,
    val artist: String?,
    val album: String?,
    val albumArtist: String?,
    val albumArtUri: String?,
    val duration: Long,                                      // milliseconds
    val trackNumber: Int?,
    val discNumber: Int?,
    val year: Int?,
    val genre: String?,
    val size: Long,                                          // bytes
    val bitrate: Int?,                                       // kbps
    val sampleRate: Int?,                                    // Hz
    val dateAdded: Long,                                     // epoch ms
    val dateModified: Long,                                  // epoch ms
    val relativePath: String?,                               // for display
    val fileName: String,
    val status: String = "ACTIVE",                           // ACTIVE / GHOST / DELETED
    val albumId: Long? = null,
    val artistId: Long? = null,
    val sourceFolderId: Long? = null
)
```

#### TrackFtsEntity

```kotlin
@Fts4(contentEntity = TrackEntity::class)
@Entity(tableName = "tracks_fts")
data class TrackFtsEntity(
    val title: String,
    val artist: String?,
    val album: String?,
    val fileName: String
)
```

> Note: Using FTS4 for maximum Room compatibility. FTS5 can be considered if Room 2.6.1 fully supports it; otherwise FTS4 provides equivalent functionality for this use case.

#### AlbumEntity

```kotlin
@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val artist: String?,                                     // album artist
    val albumArtUri: String?,
    val trackCount: Int = 0,
    val totalDuration: Long = 0,                             // ms
    val year: Int?,
    val dateAdded: Long                                      // earliest track dateAdded
)
```

#### ArtistEntity

```kotlin
@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val albumCount: Int = 0,
    val trackCount: Int = 0
)
```

#### TagEntity

```kotlin
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,                                          // ARGB
    val isSystem: Boolean = false,
    val systemType: String? = null,                          // FAVORITE / RECENT_PLAY / RECENT_ADD / MOST_PLAYED
    val createdAt: Long,
    val updatedAt: Long
)
```

#### TrackTagCrossRef

```kotlin
@Entity(
    tableName = "track_tag_cross_ref",
    primaryKeys = ["trackId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = TrackEntity::class, parentColumns = ["id"], childColumns = ["trackId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class TrackTagCrossRef(
    @ColumnInfo(index = true) val trackId: Long,
    @ColumnInfo(index = true) val tagId: Long,
    val taggedAt: Long
)
```

#### PlaylistEntity

```kotlin
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String?,
    val coverArtUri: String?,
    val createdAt: Long,
    val updatedAt: Long
)
```

#### PlaylistTrackCrossRef

```kotlin
@Entity(
    tableName = "playlist_track_cross_ref",
    primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(entity = PlaylistEntity::class, parentColumns = ["id"], childColumns = ["playlistId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TrackEntity::class, parentColumns = ["id"], childColumns = ["trackId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class PlaylistTrackCrossRef(
    @ColumnInfo(index = true) val playlistId: Long,
    @ColumnInfo(index = true) val trackId: Long,
    val orderIndex: Int,
    val addedAt: Long
)
```

#### LyricsEntity

```kotlin
@Entity(
    tableName = "lyrics",
    foreignKeys = [
        ForeignKey(entity = TrackEntity::class, parentColumns = ["id"], childColumns = ["trackId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class LyricsEntity(
    @PrimaryKey val trackId: Long,
    val content: String,                                     // raw text or LRC content
    val syncType: String,                                    // PLAIN / SYNCED
    val source: String,                                      // EMBEDDED / LRC_FILE / USER_INPUT
    val lrcFilePath: String? = null,                         // SAF URI if from .lrc file
    val updatedAt: Long
)
```

#### PlayHistoryEntity

```kotlin
@Entity(tableName = "play_history")
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val trackId: Long,
    val playedAt: Long,                                      // epoch ms
    val durationPlayed: Long                                 // ms actually listened
)
```

#### SourceFolderEntity

```kotlin
@Entity(tableName = "source_folders")
data class SourceFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val treeUri: String,                                     // SAF tree URI
    val displayName: String,
    val addedAt: Long,
    val lastScanAt: Long? = null
)
```

#### PlaybackSessionEntity

```kotlin
@Entity(tableName = "playback_session")
data class PlaybackSessionEntity(
    @PrimaryKey val id: Int = 0,                             // singleton row
    val lastTrackId: Long?,
    val lastPositionMs: Long = 0,
    val lastQueueJson: String? = null,                       // serialized queue
    val repeatMode: String = "OFF",                          // OFF / ALL / ONE
    val shuffleMode: String = "OFF",                         // OFF / ON
    val updatedAt: Long
)
```

#### CrashReportEntity

```kotlin
@Entity(tableName = "crash_reports")
data class CrashReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val throwableSummary: String,
    val stacktrace: String,
    val appVersion: String,
    val deviceInfo: String
)
```

### 8.3 Domain Models

All domain models live in `:domain:model`. They are pure Kotlin data classes with no Android dependencies.

#### Track

```kotlin
data class Track(
    val id: Long,
    val contentUri: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumArtist: String?,
    val albumArtUri: String?,
    val duration: Long,
    val trackNumber: Int?,
    val discNumber: Int?,
    val year: Int?,
    val genre: String?,
    val size: Long,
    val bitrate: Int?,
    val sampleRate: Int?,
    val dateAdded: Long,
    val dateModified: Long,
    val relativePath: String?,
    val fileName: String,
    val status: TrackStatus,
    val albumId: Long?,
    val artistId: Long?,
    val isFavorite: Boolean = false               // computed from tag system
) {
    val displayArtist: String get() = artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
    val displayAlbum: String get() = album?.takeIf { it.isNotBlank() } ?: "Unknown Album"
    val isPlayable: Boolean get() = status == TrackStatus.ACTIVE
}
```

#### Enums

```kotlin
enum class TrackStatus { ACTIVE, GHOST, DELETED }

enum class SystemTagType { FAVORITE, RECENT_PLAY, RECENT_ADD, MOST_PLAYED }

enum class SortOrder {
    TITLE_ASC, TITLE_DESC,
    ARTIST_ASC, ARTIST_DESC,
    ALBUM_ASC, ALBUM_DESC,
    DATE_ADDED_ASC, DATE_ADDED_DESC,
    DURATION_ASC, DURATION_DESC,
    YEAR_ASC, YEAR_DESC
}

enum class RepeatMode { OFF, ALL, ONE }

enum class ShuffleMode { OFF, ON }

enum class CrossfadePolicy { OVERLAP, GAPLESS, GAP }

enum class LyricsSyncType { PLAIN, SYNCED }
```

### 8.4 DataStore Keys

All in `PreferencesKeys.kt`:

```kotlin
// Appearance
val THEME_MODE = stringPreferencesKey("theme_mode")                    // SYSTEM / LIGHT / DARK
val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color")     // default: true

// Playback
val GAPLESS_ENABLED = booleanPreferencesKey("gapless_enabled")         // default: true
val CROSSFADE_DURATION_MS = intPreferencesKey("crossfade_duration_ms") // default: 0 (off)
val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")             // default: 1.0
val EQ_PRESET_ID = intPreferencesKey("eq_preset_id")                   // default: 0 (flat)
val EQ_CUSTOM_BANDS = stringPreferencesKey("eq_custom_bands")          // JSON serialized

// Library
val SORT_ORDER_TRACKS = stringPreferencesKey("sort_order_tracks")      // default: DATE_ADDED_DESC
val SORT_ORDER_ALBUMS = stringPreferencesKey("sort_order_albums")
val SORT_ORDER_ARTISTS = stringPreferencesKey("sort_order_artists")
val MIN_TRACK_DURATION_SEC = intPreferencesKey("min_track_duration")   // default: 0
val SHOW_ALBUM_ART_IN_LISTS = booleanPreferencesKey("show_album_art") // default: true
val SHOW_TRACK_NUMBERS = booleanPreferencesKey("show_track_numbers")  // default: false
val SHOW_GHOST_TRACKS = booleanPreferencesKey("show_ghost_tracks")    // default: false

// Gate
val GATE_COMPLETED = booleanPreferencesKey("gate_completed")
val TUTORIAL_SHOWN = booleanPreferencesKey("tutorial_shown")

// Search
val RECENT_SEARCHES = stringPreferencesKey("recent_searches")          // JSON array
```

---

## 9. Playback Engine

### 9.1 Architecture Overview

The playback engine resides in `:core:player` and has **zero UI dependencies**.

```
User/System Action
    → PlaybackCommand (sealed interface)
    → CommandDispatcher (Mutex serialization)
    → PlaybackStateReducer (StateFlow SSOT)
    → StackPlayerManager (Dual ExoPlayer)
    → MediaSessionService (notification + external controls)
```

### 9.2 Components

| Component | Responsibility |
|-----------|---------------|
| `StackPlayerManager` | Central coordinator: play, pause, seekTo, skip, crossfade, EQ session |
| `PlaybackQueue` | Two parallel lists: original + shuffled. AddNext, reorder, insert, remove. |
| `PlaybackStateReducer` | SSOT for `PlaybackState` (StateFlow). All reads go through here. |
| `PlaybackCommand` | Sealed interface for all commands (Play, Pause, Skip, Seek, AddNext, etc.) |
| `CommandDispatcher` | Serializes commands via Mutex. Snapshot-based execution. |
| `AudioFocusHandler` | Audio focus: gain → resume, loss → pause, duck → lower volume, transient → pause+resume |
| `CrossfadeController` | 3-branch crossfade logic |
| `EqualizerManager` | Android AudioEffect EQ integration |
| `ABRepeatController` | A/B point tracking, auto-seek on B point |

### 9.3 PlaybackState (SSOT)

```kotlin
data class PlaybackState(
    val currentTrack: Track? = null,
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val playbackSpeed: Float = 1.0f,
    val crossfadeDurationMs: Int = 0,
    val abRepeatA: Long? = null,                           // A point in ms (null = not set)
    val abRepeatB: Long? = null,                           // B point in ms (null = not set)
    val isBuffering: Boolean = false,
    val error: PlaybackError? = null
)
```

### 9.4 PlaybackCommand (Sealed Interface)

```kotlin
sealed interface PlaybackCommand {
    data class Play(val track: Track, val queue: List<Track>, val startIndex: Int) : PlaybackCommand
    data class PlayAt(val index: Int) : PlaybackCommand
    object Pause : PlaybackCommand
    object Resume : PlaybackCommand
    object SkipNext : PlaybackCommand
    object SkipPrevious : PlaybackCommand                  // 3-second rule applied
    data class SeekTo(val positionMs: Long) : PlaybackCommand
    data class AddNext(val track: Track) : PlaybackCommand
    data class AddToQueue(val track: Track) : PlaybackCommand
    data class RemoveFromQueue(val index: Int) : PlaybackCommand
    data class ReorderQueue(val from: Int, val to: Int) : PlaybackCommand
    data class SetRepeatMode(val mode: RepeatMode) : PlaybackCommand
    data class SetShuffleMode(val mode: ShuffleMode) : PlaybackCommand
    data class SetPlaybackSpeed(val speed: Float) : PlaybackCommand
    data class SetCrossfadeDuration(val durationMs: Int) : PlaybackCommand
    data class SetABRepeatA(val positionMs: Long) : PlaybackCommand
    data class SetABRepeatB(val positionMs: Long) : PlaybackCommand
    object ClearABRepeat : PlaybackCommand
    object Stop : PlaybackCommand

    // Internal commands (from player callbacks)
    data class OnTrackEnded(val endedIndex: Int) : PlaybackCommand
    data class OnError(val error: PlaybackError) : PlaybackCommand
}
```

### 9.5 Dual ExoPlayer Architecture

Two ExoPlayer instances (A / B). One is "active" (playing), the other is "warm" (pre-loaded).

**Gapless mode (`crossfadeDurationMs == 0`):**
1. Warm player pre-loads next track when active track reaches 10 seconds before end.
2. At track boundary, players swap roles. No audio gap.

**Crossfade mode (`crossfadeDurationMs > 0`):**
1. At `(trackDuration - crossfadeDuration)`, warm player starts with volume 0.
2. Active player fades from 1.0 → 0.0 over crossfade duration.
3. Warm player fades from 0.0 → 1.0 over crossfade duration.
4. At crossover complete, warm becomes active, old active is released.

**Crossfade mode (`crossfadeDurationMs < 0` — Overlap):**
1. At `(trackDuration - |crossfadeDuration|)`, warm player starts at full volume.
2. Both play simultaneously for |crossfadeDuration| milliseconds.
3. Active player stops, warm becomes active.

### 9.6 Conflict Resolution

| When Gapless is ON | Crossfade forced to 0ms |
|---------------------|------------------------|
| When Crossfade > 0 or < 0 | Gapless auto-disabled |

**Rule:** Auto-disable conflicting feature + show reason via Snackbar.

### 9.7 Shuffle & Repeat

| Feature | Behavior |
|---------|----------|
| Shuffle ON | Fisher-Yates. Current track stays at current position in shuffled list. |
| Shuffle OFF | Return to original list. Current track remains playing. |
| Repeat OFF | Queue ends at last track; playback stops. |
| Repeat ALL | After last track, wraps to first. |
| Repeat ONE | Current track loops indefinitely. |
| Previous (3s rule) | ≤3s into track → previous track. >3s → restart current. |

### 9.8 CommandDispatcher Serialization

```
1. Acquire Mutex lock
2. Read current PlaybackState (snapshot)
3. Compute new state based on command + snapshot
4. Commit new state atomically to PlaybackStateReducer
5. Execute side effects (ExoPlayer actions)
6. Release Mutex lock
```

Player callbacks (`onTrackEnded`, `onError`) are wrapped as internal `PlaybackCommand` and routed through the same dispatcher. This **guarantees** no race conditions.

### 9.9 Session Restore

- `PlaybackSessionEntity` stores: lastTrackId, lastPositionMs, queue (JSON), repeatMode, shuffleMode.
- Restore triggers **ONLY** at GateReady moment (Section 2.1).
- Session **ALWAYS** restores in Pause state.
- If lastTrackId is no longer in library → session restore silently skipped.

---

## 10. Tag System

### 10.1 System Tags (4 types, auto-managed)

| System Tag | Behavior | Refresh Trigger |
|------------|----------|-----------------|
| `FAVORITE` | User toggles heart icon | Immediate: TrackTagCrossRef insert/delete |
| `RECENT_PLAY` | Last 30 days of play history | PlayHistory write triggers async refresh |
| `RECENT_ADD` | Tracks added in last 30 days | Scan completion triggers async refresh |
| `MOST_PLAYED` | Top 100 by play count | PlayHistory write triggers async refresh |

**System tags cannot be deleted or renamed.** They are created at first app launch (seed data).

### 10.2 Custom Tags (user-managed)

- User creates tags with: name (unique), color (picker from palette).
- Tags can be assigned to any number of tracks (N:M via TrackTagCrossRef).
- Tags can be removed from tracks.
- Deleting a tag cascades: removes all TrackTagCrossRef entries.
- Tag editing: rename, change color.

### 10.3 Tag Filtering in Library

- Library tracks tab supports tag filter: user selects one or more tags.
- Default filter logic: **AND** (track must have ALL selected tags).
- Optional toggle for **OR** logic (track has ANY of selected tags).
- System tags appear first in filter list, then custom tags sorted alphabetically.

### 10.4 Favorite Integration

- Heart icon on: NowPlaying screen, MiniPlayer, Track list items, Notification (custom command).
- Tapping heart → `ToggleFavoriteUseCase` → inserts/deletes TrackTagCrossRef for FAVORITE tag.
- Track.isFavorite is a computed property: checks if FAVORITE tag exists for track.

---

## 11. Playlist System

### 11.1 CRUD Operations

| Operation | Details |
|-----------|---------|
| Create | Name (required, unique) + optional description |
| Read | List all playlists with track count + total duration |
| Update | Rename, change description, change cover art |
| Delete | Cascades: removes all PlaylistTrackCrossRef entries |

### 11.2 Track Management

- Add track to playlist → inserts PlaylistTrackCrossRef with `orderIndex = max + 1`.
- Remove track from playlist → deletes CrossRef, reindexes remaining.
- Reorder → updates orderIndex for affected entries.
- Duplicate track in same playlist: allowed (different orderIndex).

### 11.3 Playlist Playback

- Play playlist → creates queue from playlist tracks in playlist order.
- Shuffle applies to the queue, not the playlist itself.
- Long-press playlist → "Play", "Play Shuffled", "Add to Queue".

### 11.4 Playlist Detail Screen

- Header: cover art (first track's album art or custom), name, description, track count, total duration.
- Track list: drag handle for reorder, swipe-to-remove.
- FAB or action button: "Add Tracks" → opens track picker dialog.

---

## 12. Lyrics System

### 12.1 Data Sources (Priority Order)

1. **User-edited lyrics** (source = `USER_INPUT`) — highest priority
2. **LRC file** (source = `LRC_FILE`) — same-folder `.lrc` auto-matched during scan
3. **Embedded lyrics** (source = `EMBEDDED`) — extracted from audio file metadata

### 12.2 LRC Parsing

Standard LRC format support:

```
[ti:Track Title]
[ar:Artist Name]
[al:Album Name]
[00:12.34]First line of lyrics
[00:15.67]Second line of lyrics
```

- Parser extracts `List<LyricsLine>` where `LyricsLine(timestampMs: Long, text: String)`.
- Lines without timestamps → treated as plain text (LyricsSyncType.PLAIN).
- Lines with timestamps → LyricsSyncType.SYNCED.

### 12.3 NowPlaying Lyrics Display

- If lyrics exist and are SYNCED → **auto-scrolling** synced view:
  - Current line highlighted (larger font, accent color).
  - Lines scroll smoothly to keep current line centered.
  - User can tap a line to seek to that timestamp.
- If lyrics exist and are PLAIN → **static scrollable** text view.
- If no lyrics → "No lyrics available" + "Add Lyrics" button.

### 12.4 Lyrics Editor Screen

Accessed from NowPlaying "⋮" menu → "Edit Lyrics" or from "Add Lyrics" button.

**Features:**
1. **Import LRC file**: SAF file picker, filtered to `.lrc` files.
2. **Sync tool**: Play the track and tap a button at each line to set timestamps.
3. **Text editor**: Direct text input/edit of lyrics content.
4. **Export**: Save as `.lrc` file to user-selected location (SAF).

**Save behavior:** Updates `LyricsEntity` with source = `USER_INPUT`, overriding previous source.

---

## 13. Search (FTS)

### 13.1 Implementation

- Room FTS4 virtual table on `TrackFtsEntity` (title, artist, album, fileName).
- Query uses `MATCH` operator with prefix matching (`query*`).
- Results ranked by FTS relevance score.

### 13.2 Search UX

1. User taps search icon in TopBar → SearchScreen.
2. Search bar auto-focuses with keyboard.
3. **Empty state**: shows recent searches (persisted in DataStore, max 20).
4. **Typing**: 300ms debounce before firing FTS query.
5. **Results grouped by type**:
   - Tracks section (primary, always shown)
   - Albums section (matching album name)
   - Artists section (matching artist name)
6. **Result actions**:
   - Tap track → play immediately (replaces queue with search context)
   - Tap album → navigate to AlbumDetailScreen
   - Tap artist → navigate to ArtistDetailScreen
7. **Clear**: "X" button in search bar clears query, shows recent searches.
8. **Recent search management**: swipe to delete individual, "Clear all" option.

---

## 14. Equalizer

### 14.1 Architecture

- Uses Android's `android.media.audiofx.Equalizer` bound to ExoPlayer's audio session ID.
- `EqualizerManager` in `:core:player` handles lifecycle:
  - On player session created → create EQ instance.
  - On player session destroyed → release EQ.
  - On audio session ID change (player swap during crossfade) → re-bind EQ.

### 14.2 Features

- Display available frequency bands (varies by device, typically 5).
- Slider per band for gain adjustment (within device-reported min/max dB range).
- System presets: loaded from `Equalizer.getNumberOfPresets()` (e.g., Normal, Rock, Pop, Jazz).
- Custom presets: user-saved band configurations, stored in DataStore as JSON.
- "Flat" reset button: sets all bands to 0 dB.
- Enable/Disable toggle: turns EQ on/off without losing settings.

### 14.3 Persistence

- Active preset ID stored in DataStore (`EQ_PRESET_ID`).
- Custom band values stored in DataStore (`EQ_CUSTOM_BANDS`).
- On app restart, EQ is re-applied when player initializes.

---

## 15. A-B Repeat

### 15.1 Behavior

1. User taps "A" button → current playback position saved as A point.
2. Seekbar shows **A marker** at that position.
3. User taps "B" button → current position saved as B point (must be > A).
4. Seekbar shows **B marker**. Region between A and B is highlighted.
5. **Looping**: when playback reaches B → auto-seek to A. Continues until cleared.
6. User taps "Clear AB" → both points removed, normal playback resumes.

### 15.2 Rules

- A-B repeat state is part of `PlaybackState` (fields: `abRepeatA`, `abRepeatB`).
- Auto-seek at B point is routed through `CommandDispatcher` (no race conditions).
- Track change → A-B repeat automatically cleared.
- Seeking past B or before A while AB active → allowed (but loop still triggers at B).
- AB points are **not persisted** across sessions.

---

## 16. Notification & Foreground Service

### 16.1 Service Lifecycle

| State | Service | Notification |
|-------|---------|-------------|
| Playback started | Promoted to foreground | Persistent: artwork, title, artist, controls |
| Paused | Remains foreground | Notification persists with pause-state controls |
| Stopped / Queue cleared | Stops foreground | Notification dismissed |
| App killed (task removed) | Continues if playing | Notification remains |

### 16.2 StackMediaService

- Extends `MediaSessionService` (Media3).
- Creates `MediaSession` with pending intent for notification tap → opens app.
- Notification uses Media3's `MediaNotification` provider.

### 16.3 Custom Commands

| Command | Description |
|---------|-------------|
| Favorite Toggle | Toggles FAVORITE system tag; updates notification icon |
| StopClear | Stops playback AND clears queue |

### 16.4 External Control Integration

- MediaSession enables: Bluetooth, lock screen, Google Assistant control.
- Android Auto support deferred to v2.0.

---

## 17. File Deletion Flow

### 17.1 API-Specific Handling

| Android Version | Method |
|-----------------|--------|
| API 29 (Android 10) | Catch `RecoverableSecurityException` → launch IntentSender |
| API 30+ (Android 11+) | `MediaStore.createDeleteRequest()` → system confirmation dialog |
| API ≤ 28 | Direct `File.delete()` with `WRITE_EXTERNAL_STORAGE` |

### 17.2 Post-Deletion Actions

1. Remove Track from Room DB and FTS index.
2. Remove from all playlists (cascade via FK).
3. Remove all tag associations (cascade via FK).
4. Remove lyrics entry (cascade via FK).
5. If currently playing → safe transition (see below).

### 17.3 Currently-Playing Deletion Safety

1. Immediately pause current track.
2. If queue has next track → advance to next (in Pause state).
3. If queue is empty → clear player state, hide MiniPlayer, dismiss notification.
4. **No crash or ANR under any deletion scenario.**

---

## 18. Backup & Restore

### 18.1 Backup Format

SAF-selected directory. Backup creates a timestamped folder:

```
stack_backup_20260304_143000/
├── metadata.json
├── playlists.json
├── tags.json
├── settings.json
├── play_history.json
└── lyrics/
    ├── track_<id>.lrc
    └── ...
```

#### metadata.json

```json
{
    "schemaVersion": 1,
    "appVersion": "1.0.0",
    "exportedAt": "2026-03-04T14:30:00Z",
    "trackCount": 1500,
    "playlistCount": 10,
    "tagCount": 25,
    "lyricsCount": 50
}
```

### 18.2 Restore Policy

| Aspect | Policy |
|--------|--------|
| Default mode | **Merge** (not destructive overwrite) |
| Schema compatibility | Support minimum 2 previous schemaVersions |
| Tag conflict | Same name → keep existing, add new only |
| Playlist conflict | Same name → append "(Restored)" suffix |
| Settings | Overwrite with backup values |
| Lyrics | Keep existing, add missing |
| Play history | Merge (add entries not already present by timestamp) |

### 18.3 Backup UX

- Settings → Backup & Restore.
- "Create Backup" → SAF folder picker → progress dialog → success/failure snackbar.
- "Restore from Backup" → SAF folder picker → validation → merge confirmation → progress → result.

---

## 19. UI / UX Specification

### 19.1 Design Language

**"Notion + Soft Archiving"**: Clean, typography-driven, generous whitespace, monochromatic palette with a single accent color. Tag chips as primary organizational metaphor.

- Primary font: System default (Roboto on Android)
- Accent color: Indigo-600 (#4F46E5) or Dynamic Color when enabled
- Background: Surface (light) / Surface-Dark (dark mode)
- Card elevation: minimal (0-2dp)
- Corner radius: 12dp for cards, 8dp for chips, 24dp for FABs
- Spacing scale: 4, 8, 12, 16, 24, 32, 48dp

### 19.2 Adaptive Layout (WindowSizeClass)

#### Compact / Medium (Smartphone)

```
┌────────────────────────┐
│      TopBar            │
├────────────────────────┤
│                        │
│    Content Area        │
│    (scrollable)        │
│                        │
├────────────────────────┤
│    MiniPlayer          │  ← only when playing
├────────────────────────┤
│    BottomNav (4 tabs)  │
└────────────────────────┘
```

#### Expanded (Tablet / Foldable) — DualPane

```
┌────────────────────────────────────────┐
│              TopBar                    │
├───────────────────┬────────────────────┤
│                   │                    │
│   Left Pane       │   Right Pane       │
│   (Library/       │   (NowPlaying,     │
│    Search/        │    always visible)  │
│    Settings)      │                    │
│                   │                    │
├───────────────────┴────────────────────┤
│         BottomNav (4 tabs)             │
└────────────────────────────────────────┘
```

| Aspect | Compact | Expanded |
|--------|---------|----------|
| MiniPlayer | Visible when playing | **REMOVED** (Player always in right pane) |
| NowPlaying | Full-screen overlay | Right pane (always shown) |
| Back navigation | Entire screen | Left pane only; right pane fixed |
| PlaybackViewModel | `hiltViewModel()` | Same single instance for both panes |

### 19.3 PlaybackViewModel Single Instance

**Critical:** In Expanded mode, tapping a track in the left pane must instantly reflect in the right PlayerScreen.

Implementation: `activityViewModels()` scoped to `MainActivity`, or `CompositionLocalProvider` with `LocalPlaybackViewModel`. The right PlayerScreen subscribes to the same `StateFlow<PlaybackState>`.

### 19.4 Bottom Navigation (Library Tabs)

| Tab | Screen | Content |
|-----|--------|---------|
| Tracks | TracksTab | LazyColumn, sort/filter, pull-to-refresh |
| Albums | AlbumsTab | LazyVerticalGrid (2-3 cols), album art + title + artist |
| Artists | ArtistsTab | LazyColumn, artist name + album count + track count |
| Folders | FoldersTab | Breadcrumb navigation, file/folder list |

### 19.5 MiniPlayer

| Gesture | Action |
|---------|--------|
| Left/Right swipe | Change track (previous / next) |
| Tap | Navigate to NowPlaying |
| Swipe up | Expand to NowPlaying |

Content: Album art thumbnail (40dp) + title + artist (single line, ellipsis) + play/pause button + next button.

### 19.6 NowPlaying Screen

```
[↓ collapse] [⋮ menu]

┌──────────────────┐
│                  │
│   Album Art      │  ← large square, rounded corners
│   (Coil loaded)  │
│                  │
└──────────────────┘

Track Title  (marquee if overflow)
Artist Name

0:42 ──[A]──●──[B]── 3:28    ← seekbar with AB markers

🔀  ◁  ▶  ▷  🔁              ← main controls

[♥] [AB] [📋] [🎵] [⋮]       ← action row
```

Action row:
- ♥: Favorite toggle
- AB: A-B Repeat controls
- 📋: Queue sheet
- 🎵: Lyrics overlay
- ⋮: Menu (Add to playlist, Assign tag, Go to album, Go to artist, EQ, Track info, Edit lyrics)

### 19.7 Queue Sheet (BottomSheet)

- Current track highlighted with accent color.
- Drag handle on each item for reorder.
- Swipe left to remove from queue.
- Tap item → jump to that track.
- Header: "Queue" + track count + "Clear" button.

### 19.8 Track List Item

```
[AlbumArt 48dp] [TrackNo?] Title                    [♥] [⋮] [Duration]
                            Artist - Album
```

- Long press: context menu (Play, Add Next, Add to Queue, Add to Playlist, Assign Tag, Delete).
- Tap: play immediately (creates queue from current list context).

### 19.9 Default Values

| Setting | Default | Notes |
|---------|---------|-------|
| Theme | System | Follows device |
| Dynamic Color | ON | Android 12+ only |
| Sort (Tracks) | Date Added (Newest) | Per-tab persistence |
| Sort (Albums) | Name A-Z | |
| Sort (Artists) | Name A-Z | |
| Gapless | ON | Crossfade auto-off |
| Crossfade | 0 ms (OFF) | Gapless auto-off when >0 |
| Playback Speed | 1.0x | Range: 0.5x – 2.0x |
| Min Track Duration | 0 seconds | No filter |
| Show Album Art | ON | In list items |
| Show Track Numbers | OFF | |
| Show Ghost Tracks | OFF | |

---

## 20. Navigation & Deep Links

### 20.1 Route Definitions

```kotlin
sealed class NavRoute(val route: String) {
    object Gate : NavRoute("gate")
    object Main : NavRoute("main")                          // LibraryShell host
    object AlbumDetail : NavRoute("album/{albumId}")
    object ArtistDetail : NavRoute("artist/{artistId}")
    object PlaylistDetail : NavRoute("playlist/{playlistId}")
    object TagDetail : NavRoute("tag/{tagId}")
    object NowPlaying : NavRoute("now_playing")
    object Search : NavRoute("search")
    object Settings : NavRoute("settings")
    object Equalizer : NavRoute("equalizer")
    object Playlists : NavRoute("playlists")
    object Tags : NavRoute("tags")
    object BackupRestore : NavRoute("backup_restore")
    object ScanFolders : NavRoute("scan_folders")
    object LyricsEditor : NavRoute("lyrics_editor/{trackId}")
    object CrashLog : NavRoute("crash_log")
}
```

### 20.2 Deep Link Schema

| URI Pattern | Target |
|-------------|--------|
| `stack://track/{trackId}` | Play specific track |
| `stack://album/{albumId}` | AlbumDetailScreen |
| `stack://artist/{artistId}` | ArtistDetailScreen |
| `stack://playlist/{playlistId}` | PlaylistDetailScreen |
| `stack://search?q={query}` | SearchScreen with query |
| `stack://nowplaying` | NowPlayingScreen |

**GateReady compliance:** If gate is not passed, deep link target is stored as pending and executed after gate completion.

---

## 21. Settings

### 21.1 Categories

#### Appearance
- Theme: System / Light / Dark (radio selector)
- Dynamic Color: toggle (Android 12+ only, shows note on older)

#### Playback
- Gapless: toggle (auto-disables crossfade when ON)
- Crossfade: slider 0-12 seconds (shows "Gapless will be disabled" warning when moving off 0)
- Playback Speed: selector (0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x)
- Equalizer: navigates to EqualizerScreen

#### Library
- Minimum track duration: number input (seconds)
- Show album art in lists: toggle
- Show track numbers: toggle
- Show ghost tracks: toggle

#### Storage
- Scan folders: navigates to ScanFolderScreen (list + add/remove)
- Rescan library: button → full rescan with progress dialog

#### Data
- Backup: navigates to BackupRestoreScreen
- Clear play history: button with confirmation dialog
- Clear recent searches: button with confirmation dialog

#### About
- App version: display
- Open source licenses: navigates to license list (generated from dependencies)
- Crash log: navigates to CrashLogScreen

---

## 22. State Management & Architecture Patterns

### 22.1 MVI Pattern

All ViewModels follow Model-View-Intent:

```kotlin
abstract class BaseViewModel<State, Intent, Effect>(
    initialState: State
) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    fun dispatch(intent: Intent) { /* process intent, update state, emit effects */ }

    protected fun updateState(reducer: State.() -> State) {
        _state.update(reducer)
    }

    protected suspend fun emitEffect(effect: Effect) {
        _effect.send(effect)
    }
}
```

### 22.2 Result Pattern

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

### 22.3 Concurrency Rules

| Domain | Strategy | Mechanism |
|--------|----------|-----------|
| Player state | Serialized mutations | Mutex + snapshot in CommandDispatcher |
| Scan operations | Single pipeline | Mutex-based merge in ScanManager |
| DB writes | Room transaction | Room's thread safety + suspend functions |
| UI state | Main dispatcher | StateFlow on Dispatchers.Main |
| High-risk ops | Progress + Cancel | Coroutine Job cancellation + UI progress |

---

## 23. Diagnostics & Stability

### 23.1 In-App Crash Capture

- `CrashCapture` sets `Thread.setDefaultUncaughtExceptionHandler`.
- On crash: saves stacktrace, throwable summary, app version, device info, timestamp to `CrashReportEntity`.
- Then delegates to default handler (app terminates).
- CrashLogScreen: list of recent crashes (newest first), expandable stacktrace detail.
- Max stored crashes: 50. Oldest auto-pruned.

### 23.2 Logging

- `Logger` wrapper with tag support, backed by Timber.
- Debug builds: Logcat output.
- Release builds: optionally route to rotating local file (max 5MB × 3 files).
- Log levels: DEBUG, INFO, WARN, ERROR.

### 23.3 Privacy

- **No INTERNET permission** (unless explicitly added in future).
- All logs and crash reports are **local-only**.
- No telemetry, no analytics, no external data transmission.
- SAF-based storage access: no unnecessary broad storage permissions.

---

## 24. Internationalization (i18n)

### 24.1 Supported Languages

| Language | Resource Directory | Status |
|----------|-------------------|--------|
| English | `values/strings.xml` | Default (fallback) |
| Korean | `values-ko/strings.xml` | Full translation |
| Japanese | `values-ja/strings.xml` | Full translation |

### 24.2 Rules

- **Zero hardcoded user-facing strings** in Kotlin/Compose code.
- All strings referenced via `stringResource(R.string.*)`.
- Plurals use `pluralStringResource()`.
- Date/time formatting uses `DateTimeUtil` with locale-aware formatters.
- Sort order labels are localized.
- System tag names are localized (FAVORITE → "즐겨찾기" / "お気に入り").

---

## 25. Non-Functional Requirements

| Category | Requirement | Target |
|----------|-------------|--------|
| Stability | Zero crash/ANR in critical paths | 0 critical crashes per 1000 sessions |
| Performance | Large library support | Smooth at 10,000+ tracks |
| Performance | Cold start | < 2 seconds to GateReady check |
| Performance | Search latency | < 200ms for FTS query |
| Performance | Scan speed | 1,000 tracks/minute minimum |
| Battery | Scan/artwork processing | Batch + throttle, no idle wake locks |
| Memory | Image cache | 256 MB Coil disk LRU cache |
| Data Integrity | All mutations | DB consistency across all paths |
| UX | Design consistency | Unified Notion-like tokens throughout |
| Accessibility | TalkBack | All interactive elements have contentDescription |
| Security | Permissions | Minimal: only audio, notifications, no INTERNET |

---

## 26. Acceptance Criteria

### 26.1 Gate & Permissions
- [ ] GateReady NOT satisfied → Library, Player, Search all invisible
- [ ] GateReady first transition → session restores paused + scan starts
- [ ] Permission revoked at runtime → transitions to Not Ready with recovery UI
- [ ] Folder added → incremental scan triggers
- [ ] Folder removed → affected tracks become GHOST

### 26.2 Library
- [ ] All 4 tabs display correct content with sorting
- [ ] Pull-to-refresh triggers incremental scan
- [ ] Album/Artist detail screens show correct track listings
- [ ] Folder browser navigates breadcrumb correctly
- [ ] Empty states shown when appropriate

### 26.3 Playback
- [ ] Crossfade 3-branch (overlap/gapless/gap) all produce stable audio
- [ ] Queue/Order separation: AddNext, Shuffle, Repeat never corrupt queue
- [ ] Race condition test: rapid Skip + Seek + AddNext → no crash/corruption
- [ ] Session restore after process death: correct track, position, queue state
- [ ] A-B repeat loops correctly at B → A, clears on track change
- [ ] Equalizer settings apply audibly and persist across restarts

### 26.4 Tags
- [ ] System tags (4) auto-created on first launch
- [ ] FAVORITE toggle works from: NowPlaying, MiniPlayer, Track list, Notification
- [ ] RECENT_PLAY/RECENT_ADD/MOST_PLAYED auto-refresh correctly
- [ ] Custom tag CRUD works without affecting system tags
- [ ] Tag filter in library produces correct results (AND/OR)

### 26.5 Playlists
- [ ] Create/Read/Update/Delete all work
- [ ] Track add, remove, reorder within playlist
- [ ] Playing playlist creates correct queue
- [ ] Deleting track from library removes from all playlists

### 26.6 Lyrics
- [ ] Embedded lyrics display correctly
- [ ] .lrc auto-match works for files in same folder
- [ ] Synced lyrics scroll in time with playback
- [ ] Tap on lyric line seeks to that timestamp
- [ ] User can import external .lrc, edit text, and export

### 26.7 Search
- [ ] FTS returns relevant results for title/artist/album/fileName
- [ ] 300ms debounce prevents excessive queries
- [ ] Recent searches persist and display
- [ ] Result tap plays track or navigates to detail

### 26.8 Notification & Service
- [ ] Notification appears only when playback starts
- [ ] Pause state: notification persists with correct controls
- [ ] Custom commands (Favorite, StopClear) work from notification
- [ ] Bluetooth/lock screen controls work via MediaSession

### 26.9 File Deletion
- [ ] API 29: RecoverableSecurityException flow works
- [ ] API 30+: createDeleteRequest flow works
- [ ] Currently-playing deletion: safe transition to next track

### 26.10 Backup & Restore
- [ ] Backup creates valid JSON files at SAF-selected location
- [ ] Restore merges data correctly without duplicates
- [ ] Schema version mismatch shows appropriate error

### 26.11 Settings
- [ ] Theme changes apply immediately
- [ ] Gapless/Crossfade conflict resolution works with user notification
- [ ] Rescan triggers full library refresh with progress
- [ ] All settings persist across restarts

### 26.12 Adaptive UI
- [ ] Compact: MiniPlayer visible, NowPlaying full-screen
- [ ] Expanded DualPane: right pane PlayerScreen fixed, no MiniPlayer
- [ ] Left pane interaction instantly reflected in right Player

### 26.13 i18n
- [ ] All strings localized in EN, KO, JA
- [ ] No hardcoded user-facing strings in code
- [ ] Locale change applies immediately

---

## 27. Development Phases

### Phase 1: Foundation
- Project scaffolding (multi-module, Gradle KTS, Version Catalog)
- Core utilities (Result, Logger, CrashCapture, Dispatchers, Theme)
- Hilt DI setup across all modules
- Base MVI architecture (BaseViewModel)

### Phase 2: Data Layer
- Room database (all 13 entities + DAOs)
- FTS virtual table
- Entity ↔ Domain mappers
- DataStore preferences
- All Repository interfaces + implementations
- DI modules for database and repositories

### Phase 3: Scan System
- MediaStoreScanner (metadata extraction)
- LrcFileScanner (.lrc discovery)
- ScanManager (Mutex-based pipeline)
- ContentObserverManager (change detection + debounce)
- ScanLibraryUseCase, RescanUseCase, CleanGhostTracksUseCase

### Phase 4: Gate & Onboarding
- GateScreen (Permission deck card flow)
- PermissionManager (API-level-aware)
- SAF folder selection + persistence
- GateReady state machine
- Scan progress UI

### Phase 5: Playback Engine
- StackPlayerManager (Dual ExoPlayer)
- PlaybackQueue (original + shuffled lists)
- PlaybackStateReducer (StateFlow SSOT)
- CommandDispatcher (Mutex serialization)
- AudioFocusHandler
- CrossfadeController (3-branch)
- Session save/restore

### Phase 6: Core UI
- MainActivity + StackNavHost
- LibraryShellScreen (BottomNav 4 tabs + TopBar)
- TracksTab, AlbumsTab, ArtistsTab, FoldersTab
- AlbumDetailScreen, ArtistDetailScreen
- MiniPlayer
- NowPlayingScreen + QueueSheet
- StackMediaService + notification

### Phase 7: Tags & Playlists
- Tag system (entities, UI, system tag auto-refresh)
- TagsScreen, TagDetailScreen, TagEditorDialog
- Playlist CRUD (entities, UI, track management)
- PlaylistsScreen, PlaylistDetailScreen, PlaylistEditorDialog

### Phase 8: Search, Lyrics, EQ, A-B Repeat
- SearchScreen (FTS + recent searches)
- LyricsOverlay (synced scroll)
- LyricsEditorScreen (import/edit/export)
- EqualizerScreen (bands + presets)
- ABRepeatControls (seekbar markers + loop)

### Phase 9: Settings, Backup, Deletion, Deep Links
- SettingsScreen (all categories)
- ScanFolderScreen
- BackupRestoreScreen
- File deletion flow (API 29/30+)
- Deep link handling

### Phase 10: Polish & i18n
- Korean string resources
- Japanese string resources
- Adaptive UI (Expanded DualPane)
- CrashLogScreen
- Tutorial overlay (first launch)
- Final UI polish, accessibility audit
- Performance testing (10,000+ tracks)

---

## 28. v2.0+ Roadmap

The following features are **explicitly excluded from v1.0** and deferred:

| Feature | Target |
|---------|--------|
| Sleep Timer | v2.0 |
| Android Auto | v2.0 |
| Home Screen Widget (Glance) | v2.0 |
| Smart Playlists (criteria-based auto) | v2.0 |
| Metadata/Tag Editor (write to file) | v2.1 |
| Speed/Pitch separation | v2.1 |
| Paging 3 integration | v2.0 (if performance requires) |
| WorkManager background scan | v2.0 |
| Cloud backup (Google Drive) | v3.0 |

---

## Appendix A: Gradle Build Configuration

### Root build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
}
```

### settings.gradle.kts

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Stack"

include(":app")
include(":core")
include(":core:player")
include(":data")
include(":domain")
include(":feature:gate")
include(":feature:library")
include(":feature:player")
include(":feature:search")
include(":feature:tags")
include(":feature:playlists")
include(":feature:settings")
include(":feature:crashlog")
```

### libs.versions.toml (key sections)

```toml
[versions]
agp = "8.7.3"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
coreKtx = "1.15.0"
appcompat = "1.7.0"
activityCompose = "1.9.3"
lifecycleRuntimeKtx = "2.8.7"
composeBom = "2024.12.01"
navigation = "2.8.5"
hilt = "2.56.2"
hiltNavigationCompose = "1.2.0"
room = "2.6.1"
datastore = "1.1.3"
media3 = "1.5.1"
coil = "3.0.4"
coroutines = "1.9.0"
junit = "4.13.2"
junitVersion = "1.2.1"
espressoCore = "3.6.1"

[libraries]
# AndroidX Core
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeKtx" }

# Compose
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }

# Navigation
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# DataStore
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Media3
media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
media3-session = { group = "androidx.media3", name = "media3-session", version.ref = "media3" }
media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }

# Coil
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }

# Coroutines
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

# Debug
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
room = { id = "androidx.room", version.ref = "room" }
```

---

## Appendix B: AndroidManifest Permissions

```xml
<!-- Required -->
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />           <!-- API 33+ -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />                                                  <!-- API ≤32 -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />           <!-- API 33+ -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- For file deletion -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />                                                  <!-- API ≤28 only -->

<!-- Explicitly NOT included -->
<!-- No INTERNET permission — privacy-first, local-only app -->
```

---

**End of SSOT Document**

*Document version: 4.0*
*Last updated: 2026-03-04*
*Status: AUTHORITATIVE — All implementation must conform to this document.*
