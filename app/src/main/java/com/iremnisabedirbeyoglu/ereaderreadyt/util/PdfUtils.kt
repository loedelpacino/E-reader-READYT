import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** PDF'yi sayfa sayfa Bitmap'lere rasterize eder (basit sürüm). */
suspend fun loadPdfBitmaps(contentResolver: ContentResolver, uri: Uri): List<Bitmap> {
    return withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()

        val pfd: ParcelFileDescriptor? = contentResolver.openFileDescriptor(uri, "r")
        pfd?.use {
            val renderer = PdfRenderer(it)
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val width = page.width
                val height = page.height
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                page.close()
            }
            renderer.close()
        }

        bitmaps
    }
}

/** Seçilen PDF’in görünen dosya adını verir (ör. "Sefiller.pdf"). */
fun getDisplayName(resolver: ContentResolver, uri: Uri): String? {
    var name: String? = null
    val cursor = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            name = it.getString(0)
        }
    }
    return name
}

