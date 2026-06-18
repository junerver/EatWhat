package com.eatwhat.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 主题模式
 */
enum class ThemeMode {
  SYSTEM,  // 跟随系统
  LIGHT,   // 浅色
  DARK     // 深色
}

/**
 * 主题偏好设置管理
 */
class ThemePreferences(private val context: Context) {
  companion object {
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
  }

  val themeModeFlow: Flow<ThemeMode> = context.dataStore.data
    .map { preferences ->
      when (preferences[THEME_MODE_KEY]) {
        ThemeMode.LIGHT.name -> ThemeMode.LIGHT
        ThemeMode.DARK.name -> ThemeMode.DARK
        else -> ThemeMode.SYSTEM
      }
    }

  suspend fun setThemeMode(mode: ThemeMode) {
    context.dataStore.edit { preferences ->
      preferences[THEME_MODE_KEY] = mode.name
    }
  }
}
