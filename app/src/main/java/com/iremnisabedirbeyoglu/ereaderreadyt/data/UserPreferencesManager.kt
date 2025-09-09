// path: app/src/main/java/com/iremnisabedirbeyoglu/ereaderreadyt/data/UserPreferencesManager.kt
package com.iremnisabedirbeyoglu.ereaderreadyt.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Bu dosyaya özel DataStore (diğer dosyadakiyle isim çakışmasın diye userPrefs kullandık)
private val Context.userPrefs by preferencesDataStore(name = "user_prefs")

object UserPreferencesManager {

    // ----- Görsel tercih anahtarları -----
    private val FONT_SIZE_KEY = stringPreferencesKey("font_size")              // small|medium|large...
    private val BACKGROUND_COLOR_KEY = stringPreferencesKey("background_color")// #FAF8F6 ...
    private val FONT_STYLE_KEY = stringPreferencesKey("font_style")            // Lora, ...
    private val FONT_WEIGHT_KEY = stringPreferencesKey("font_weight")          // normal|bold
    private val FONT_ITALIC_KEY = stringPreferencesKey("font_italic")          // normal|italic

    // ----- Getter Flow'ları (varsayılanlarla güvenli) -----
    fun getFontSize(context: Context): Flow<String> =
        context.userPrefs.data.map { it[FONT_SIZE_KEY] ?: "medium" }

    fun getBackgroundColor(context: Context): Flow<String> =
        context.userPrefs.data.map { it[BACKGROUND_COLOR_KEY] ?: "#FAF8F6" }

    fun getFontStyle(context: Context): Flow<String> =
        context.userPrefs.data.map { it[FONT_STYLE_KEY] ?: "Lora" }

    fun getFontWeight(context: Context): Flow<String> =
        context.userPrefs.data.map { it[FONT_WEIGHT_KEY] ?: "normal" }

    fun getFontItalic(context: Context): Flow<String> =
        context.userPrefs.data.map { it[FONT_ITALIC_KEY] ?: "normal" }

    // ----- Setter'lar -----
    suspend fun setFontSize(context: Context, size: String) {
        context.userPrefs.edit { it[FONT_SIZE_KEY] = size }
    }

    suspend fun setBackgroundColor(context: Context, color: String) {
        context.userPrefs.edit { it[BACKGROUND_COLOR_KEY] = color }
    }

    suspend fun setFontStyle(context: Context, font: String) {
        context.userPrefs.edit { it[FONT_STYLE_KEY] = font }
    }

    suspend fun setFontWeight(context: Context, weight: String) {
        context.userPrefs.edit { it[FONT_WEIGHT_KEY] = weight }
    }

    suspend fun setFontItalic(context: Context, style: String) {
        context.userPrefs.edit { it[FONT_ITALIC_KEY] = style }
    }

    // ----- Her PDF için son sayfa kaydı -----
    private fun lastPageKeyFor(uri: String) = intPreferencesKey("last_page_${uri.hashCode()}")

    fun getLastPage(context: Context, uri: String): Flow<Int> =
        context.userPrefs.data.map { prefs -> prefs[lastPageKeyFor(uri)] ?: 0 }

    suspend fun setLastPage(context: Context, uri: String, pageIndex: Int) {
        context.userPrefs.edit { prefs ->
            prefs[lastPageKeyFor(uri)] = pageIndex.coerceAtLeast(0)
        }
    }

    // (Opsiyonel) hepsini temizleme
    suspend fun clearAll(context: Context) {
        context.userPrefs.edit { it.clear() }
    }
}
