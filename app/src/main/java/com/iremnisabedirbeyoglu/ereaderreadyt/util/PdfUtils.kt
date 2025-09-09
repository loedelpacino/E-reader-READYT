// path: app/src/main/java/com/iremnisabedirbeyoglu/ereaderreadyt/util/PdfUtils.kt
package com.iremnisabedirbeyoglu.ereaderreadyt.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * PDF'yi sayfa sayfa Bitmap'lere rasterize eder (basit sürüm).
 * UYARI: Büyük PDF'lerde tüm sayfaları RAM'e almak OOM riski yaratabilir.
 * Bu yüzden aşağıdaki overload'ı (maxWidth) tercih etmeni öneririm.
 */
suspend fun loadPdfBitmaps(contentResolver: ContentResolver, uri: Uri): List<Bitmap> =
    loadPdfBitmaps(contentResolver, uri, maxWidth = 1600)

/**
 * İyileştirilmiş sürüm: her sayfayı verilen genişlikte ölçekleyerek render eder.
 * @param maxWidth: hedef genişlik (px). Oran korunur. Varsayılan 1600.
 */
suspend fun loadPdfBitmaps(
    contentResolver: ContentResolver,
    uri: Uri,
    maxWidth: Int
): List<Bitmap> = withContext(Dispatchers.IO) {
    val result = mutableListOf<Bitmap>()
    var pfd: ParcelFileDescriptor? = null
    var renderer: PdfRenderer? = null
    try {
        pfd = contentResolver.openFileDescriptor(uri, "r")
        if (pfd != null) {
            renderer = PdfRenderer(pfd)
            val pageCount = renderer.pageCount
            for (i in 0 until pageCount) {
                renderer.openPage(i).use { page ->
                    // Ölçek: sayfa genişliğini maxWidth'e sığdır
                    val scale = if (page.width > 0) {
                        (maxWidth.toFloat() / page.width.toFloat()).coerceAtMost(1f)
                    } else 1f
                    val targetW = (page.width * scale).toInt().coerceAtLeast(1)
                    val targetH = (page.height * scale).toInt().coerceAtLeast(1)

                    val bmp = Bitmap.createBitmap(
                        targetW,
                        targetH,
                        Bitmap.Config.ARGB_8888
                    )
                    // Clip vererek render alanını tam hedef boyuta çekiyoruz
                    page.render(
                        bmp,
                        Rect(0, 0, targetW, targetH),
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )
                    result.add(bmp)
                }
            }
        }
    } finally {
        runCatching { renderer?.close() }
        runCatching { pfd?.close() }
    }
    result
}

/**
 * Seçilen PDF’in görünen dosya adını verir (ör. "Sefiller.pdf").
 * - Önce OpenableColumns.DISPLAY_NAME dener
 * - Olmazsa path'ten decode edilip son segment döner
 * - En sonda "PDF" döner (null değil)
 */
fun getDisplayName(resolver: ContentResolver, uri: Uri): String {
    // 1) İçerik sağlayıcıdan
    try {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val name = c.getString(0)
                if (!name.isNullOrBlank()) return name
            }
        }
    } catch (_: SecurityException) {
        // Kalıcı izin yoksa veya sağlayıcı kısıtlıysa path'e düşeceğiz
    } catch (_: Exception) {
        // Sessiz geç
    }

    // 2) Path segmentinden
    val raw = uri.lastPathSegment
    if (!raw.isNullOrBlank()) {
        val decoded = runCatching {
            URLDecoder.decode(raw.substringAfterLast('/'), StandardCharsets.UTF_8.name())
        }.getOrElse { raw.substringAfterLast('/') }
        if (decoded.isNotBlank()) return decoded
    }

    // 3) Fallback
    return "PDF"
}
