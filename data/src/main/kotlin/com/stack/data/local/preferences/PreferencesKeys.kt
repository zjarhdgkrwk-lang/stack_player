package com.stack.data.local.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    // Appearance
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color")

    // Playback
    val GAPLESS_ENABLED = booleanPreferencesKey("gapless_enabled")
    val CROSSFADE_DURATION_MS = intPreferencesKey("crossfade_duration_ms")
    val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
    val EQ_PRESET_ID = intPreferencesKey("eq_preset_id")
    val EQ_CUSTOM_BANDS = stringPreferencesKey("eq_custom_bands")

    // Library
    val SORT_ORDER_TRACKS = stringPreferencesKey("sort_order_tracks")
    val SORT_ORDER_ALBUMS = stringPreferencesKey("sort_order_albums")
    val SORT_ORDER_ARTISTS = stringPreferencesKey("sort_order_artists")
    val MIN_TRACK_DURATION_SEC = intPreferencesKey("min_track_duration")
    val SHOW_ALBUM_ART_IN_LISTS = booleanPreferencesKey("show_album_art")
    val SHOW_TRACK_NUMBERS = booleanPreferencesKey("show_track_numbers")
    val SHOW_GHOST_TRACKS = booleanPreferencesKey("show_ghost_tracks")

    // Gate
    val GATE_COMPLETED = booleanPreferencesKey("gate_completed")
    val TUTORIAL_SHOWN = booleanPreferencesKey("tutorial_shown")

    // Search
    val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
}
