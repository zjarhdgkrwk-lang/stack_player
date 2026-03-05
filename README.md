# Stack

**Android local music player with Notion-style UI and tag-based organization.**

## Overview

Stack is a local music archive app that organizes folder/file-based music libraries
through tags, playlists, and powerful search, with a clean Notion-inspired aesthetic.

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture + MVI |
| DI | Hilt |
| Database | Room |
| Playback | Media3 (ExoPlayer) |
| Image | Coil 3 |

## Documentation

- [Technical Specification (SSOT)](docs/Stack_SSOT.md)
- [Claude Code Prompts Guide](docs/Claude_Code_Prompts.md)

## Project Structure
stack-player/
├── app/                    # Application entry point
├── core/                   # Common utilities, theme
│   └── player/             # Playback engine
├── data/                   # Room, DataStore, Scanner
├── domain/                 # UseCases, models, interfaces
└── feature/
├── gate/               # Onboarding
├── library/            # Library browsing
├── player/             # NowPlaying UI
├── search/             # Search
├── tags/               # Tag management
├── playlists/          # Playlist management
├── settings/           # Settings
└── crashlog/           # Crash viewer
## Development

### Prerequisites

- Android Studio Ladybug (2024.2) or newer
- JDK 17
- Android SDK 35

### Build
