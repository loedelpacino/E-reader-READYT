package com.iremnisabedirbeyoglu.ereaderreadyt.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPreferencesManager {
    private val FONT_SIZE_KEY = stringPreferencesKey("font_size")
    private val BACKGROUND_COLOR_KEY = stringPreferencesKey("background_color")
    private val FONT_STYLE_KEY = stringPreferencesKey("font_style")

    fun getFontSize(context: Context): Flow<String> =
        context.dataStore.data.map { it[FONT_SIZE_KEY] ?: "medium" }

    fun getBackgroundColor(context: Context): Flow<String> =
        context.dataStore.data.map { it[BACKGROUND_COLOR_KEY] ?: "#FAF8F6" }

    fun getFontStyle(context: Context): Flow<String> =
        context.dataStore.data.map { it[FONT_STYLE_KEY] ?: "Lora" }

    suspend fun setFontSize(context: Context, size: String) {
        context.dataStore.edit { it[FONT_SIZE_KEY] = size }
    }

    suspend fun setBackgroundColor(context: Context, color: String) {
        context.dataStore.edit { it[BACKGROUND_COLOR_KEY] = color }
    }

    suspend fun setFontStyle(context: Context, font: String) {
        context.dataStore.edit { it[FONT_STYLE_KEY] = font }
    }
}
