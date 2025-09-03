package com.iremnisabedirbeyoglu.ereaderreadyt.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPreferencesManager {
    private val FONT_SIZE_KEY = stringPreferencesKey("font_size")
    private val BACKGROUND_COLOR_KEY = stringPreferencesKey("background_color")
    private val FONT_STYLE_KEY = stringPreferencesKey("font_style")
    private val FONT_WEIGHT_KEY = stringPreferencesKey("font_weight")   // normal | bold
    private val FONT_ITALIC_KEY = stringPreferencesKey("font_italic")   // normal | italic

    fun getFontSize(context: Context): Flow<String> =
        context.dataStore.data.map { it[FONT_SIZE_KEY] ?: "medium" }

    fun getBackgroundColor(context: Context): Flow<String> =
        context.dataStore.data.map { it[BACKGROUND_COLOR_KEY] ?: "#FAF8F6" }

    fun getFontStyle(context: Context): Flow<String> =
        context.dataStore.data.map { it[FONT_STYLE_KEY] ?: "Lora" }

    fun getFontWeight(context: Context): Flow<String> =
        context.dataStore.data.map { it[FONT_WEIGHT_KEY] ?: "normal" }

    fun getFontItalic(context: Context): Flow<String> =
        context.dataStore.data.map { it[FONT_ITALIC_KEY] ?: "normal" }

    suspend fun setFontSize(context: Context, size: String) {
        context.dataStore.edit { it[FONT_SIZE_KEY] = size }
    }

    suspend fun setBackgroundColor(context: Context, color: String) {
        context.dataStore.edit { it[BACKGROUND_COLOR_KEY] = color }
    }

    suspend fun setFontStyle(context: Context, font: String) {
        context.dataStore.edit { it[FONT_STYLE_KEY] = font }
    }

    suspend fun setFontWeight(context: Context, weight: String) {
        context.dataStore.edit { it[FONT_WEIGHT_KEY] = weight }
    }

    suspend fun setFontItalic(context: Context, style: String) {
        context.dataStore.edit { it[FONT_ITALIC_KEY] = style }
    }

    // --- EK: her PDF için son sayfa ---
    private fun lastPageKeyFor(uri: String) =
        intPreferencesKey("last_page_${uri.hashCode()}")

    fun getLastPage(context: Context, uri: String) =
        context.dataStore.data.map { prefs ->
            prefs[lastPageKeyFor(uri)] ?: 0
        }

    suspend fun setLastPage(context: Context, uri: String, pageIndex: Int) {
        context.dataStore.edit { prefs ->
            prefs[lastPageKeyFor(uri)] = pageIndex.coerceAtLeast(0)
        }
    }

}

