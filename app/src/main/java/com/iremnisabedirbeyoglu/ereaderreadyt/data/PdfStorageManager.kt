package com.iremnisabedirbeyoglu.ereaderreadyt.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pdf_storage")

object PdfStorageManager {
    private val PDF_LIST_KEY = stringSetPreferencesKey("pdf_uri_list")

    // URI’leri kaydet
    suspend fun addPdfUri(context: Context, uri: Uri) {
        context.dataStore.edit { preferences ->
            val currentUris = preferences[PDF_LIST_KEY] ?: emptySet()
            preferences[PDF_LIST_KEY] = currentUris + uri.toString()
        }
    }

    // URI’leri oku
    fun getPdfUriList(context: Context): Flow<List<Uri>> {
        return context.dataStore.data.map { preferences ->
            preferences[PDF_LIST_KEY]?.map { Uri.parse(it) } ?: emptyList()
        }
    }

    // (İsteğe bağlı) URI’yi sil
    suspend fun removePdfUri(context: Context, uri: Uri) {
        context.dataStore.edit { preferences ->
            val currentUris = preferences[PDF_LIST_KEY] ?: emptySet()
            preferences[PDF_LIST_KEY] = currentUris - uri.toString()
        }
    }

    // (İsteğe bağlı) Hepsini temizle
    suspend fun clearAll(context: Context) {
        context.dataStore.edit { it.clear() }
    }
}
