package com.iremnisabedirbeyoglu.ereaderreadyt.data

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Tek örnek DataStore: app geneli için güvenli
private val Context.dataStore by preferencesDataStore(name = "pdf_storage")

/**
 * PDF URI listesini ve "son okunan" PDF'yi DataStore'da tutar.
 * Var olan API'leri bozmadan, persistable URI izni ve ufak guard'lar eklendi.
 */
object PdfStorageManager {
    private val PDF_LIST_KEY = stringSetPreferencesKey("pdf_uri_list")
    private val LAST_READ_KEY = stringPreferencesKey("last_read_pdf")

    // ---- KAMU API (mevcutları koruduk) ----

    /** URI ekle (set olduğundan tekrar eklenirse sorun olmaz). */
    suspend fun addPdfUri(context: Context, uri: Uri) {
        context.dataStore.edit { preferences ->
            val currentUris = preferences[PDF_LIST_KEY].orEmpty()
            if (uri.toString().isNotBlank()) {
                preferences[PDF_LIST_KEY] = currentUris + uri.toString()
            }
        }
    }

    /** Kayıtlı URI'leri oku (liste olarak). */
    fun getPdfUriList(context: Context): Flow<List<Uri>> {
        return context.dataStore.data.map { preferences ->
            preferences[PDF_LIST_KEY]
                ?.asSequence()
                ?.filter { it.isNotBlank() }
                ?.map { Uri.parse(it) }
                ?.toList()
                ?: emptyList()
        }
    }

    /** Son okunan PDF'yi kaydet. */
    suspend fun setLastReadPdf(context: Context, uri: Uri) {
        context.dataStore.edit { prefs ->
            prefs[LAST_READ_KEY] = uri.toString()
        }
    }

    /** Son okunan PDF'yi getir. */
    fun getLastReadPdf(context: Context): Flow<Uri?> {
        return context.dataStore.data.map { prefs ->
            prefs[LAST_READ_KEY]?.takeIf { it.isNotBlank() }?.let(Uri::parse)
        }
    }

    /** URI sil. */
    suspend fun removePdfUri(context: Context, uri: Uri) {
        context.dataStore.edit { preferences ->
            val currentUris = preferences[PDF_LIST_KEY].orEmpty()
            preferences[PDF_LIST_KEY] = currentUris - uri.toString()
        }
    }

    /** Tüm verileri temizle. */
    suspend fun clearAll(context: Context) {
        context.dataStore.edit { it.clear() }
    }

    // ---- YARDIMCILAR (izin ve emniyet) ----

    /**
     * Kalıcı okuma izni al (OpenDocument ile seçilen dosyalar için önerilir).
     * Bu fonksiyonu PDF'yi ilk eklerken çağır:
     *
     * resolver.takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
     */
    fun takePersistableReadPermission(resolver: ContentResolver, uri: Uri) {
        try {
            resolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // İzin zaten alınmış olabilir ya da sağlayıcı izin vermeyebilir; sessiz geçiyoruz.
        } catch (_: Exception) {
        }
    }

    /**
     * Mevcut izin büyük oranda ContentResolver üzerinde track edilmediği için
     * kesin kontrol yapmak zordur; ancak "deneyip yakala" yaklaşımı kullanan
     * kodlarda bu yardımcıyı çağırmak yeterli olur.
     * İhtiyaç halinde üst katmanda bir "smoke test" (stream open) ile doğrulayabilirsin.
     */
    fun hasLikelyPermission(uri: Uri): Boolean {
        // ContentResolver seviyesinde güvenilir bir API yok;
        // burayı şimdilik uri scheme kontrolü ile sınırlıyoruz.
        return uri.scheme == "content" || uri.scheme == "file"
    }

    /**
     * Güvenli ekleme: Kalıcı izin almayı dener, sonra URI'yi kaydeder.
     * Kullanmak istersen:
     * PdfStorageManager.addPdfSafely(context, resolver, pickedUri)
     */
    suspend fun addPdfSafely(context: Context, resolver: ContentResolver, uri: Uri) {
        takePersistableReadPermission(resolver, uri)
        addPdfUri(context, uri)
    }
}
