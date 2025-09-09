// path: app/src/main/java/com/iremnisabedirbeyoglu/ereaderreadyt/screens/BookReaderScreen.kt
package com.iremnisabedirbeyoglu.ereaderreadyt.screens

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.LruCache
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.iremnisabedirbeyoglu.ereaderreadyt.data.PdfStorageManager
import com.iremnisabedirbeyoglu.ereaderreadyt.data.UserPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class ScrollMode { Vertical, Horizontal }
private const val EDGE_FRACTION = 0.15f // tek dokunuşla sayfa çevirme için kenar oranı

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReaderScreen(
    uriString: String?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    // Ekran uykuya geçmesin
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    if (uriString == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("PDF bulunamadı.") }
        return
    }
    val uri = remember(uriString) { Uri.parse(uriString) }

    val surface = Color.White

    // PdfRenderer durumları
    var pageCount by remember { mutableStateOf(0) }
    var displayName by remember { mutableStateOf<String?>(null) }
    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pfd by remember { mutableStateOf<ParcelFileDescriptor?>(null) }

    // Refresh tetikleyicisi
    var refreshTick by rememberSaveable { mutableStateOf(0) }

    // Kaldığın yer
    val savedPage by UserPreferencesManager.getLastPage(context, uriString).collectAsState(initial = 0)

    // PDF’yi aç / yeniden aç
    LaunchedEffect(uri, refreshTick) {
        // önce eskileri kapat
        runCatching { renderer?.close() }
        runCatching { pfd?.close() }
        renderer = null
        pfd = null
        pageCount = 0

        displayName = safeGetDisplayName(context.contentResolver, uri) ?: "PDF"
        context.contentResolver.openFileDescriptor(uri, "r")?.let { fd ->
            pfd = fd
            renderer = PdfRenderer(fd)
            pageCount = renderer?.pageCount ?: 0
        }
        PdfStorageManager.setLastReadPdf(context, uri)
    }
    DisposableEffect(uri) {
        onDispose {
            runCatching { renderer?.close() }
            runCatching { pfd?.close() }
        }
    }

    // Okuma modu
    var mode by rememberSaveable { mutableStateOf(ScrollMode.Horizontal) }
    val listState = rememberLazyListState()
    val pagerState = rememberPagerState(pageCount = { pageCount })
    var currentPage by rememberSaveable { mutableStateOf(0) }

    // UI chrome görünürlüğü
    var chromeVisible by rememberSaveable { mutableStateOf(false) }

    // Render genişliği (px)
    val screenPx = with(LocalConfiguration.current) {
        (screenWidthDp * context.resources.displayMetrics.density).toInt()
    }

    // Zoom & Pan
    var scale by rememberSaveable { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var displayedSize by remember { mutableStateOf(IntSize.Zero) }
    fun resetZoom() { scale = 1f; offset = Offset.Zero }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val nextScale = (scale * zoomChange).coerceIn(1f, 4f)
        val nextOffset = if (nextScale > 1f) offset + panChange else Offset.Zero
        scale = nextScale
        offset = if (displayedSize != IntSize.Zero)
            clampOffset(nextOffset, displayedSize, displayedSize, nextScale)
        else nextOffset
    }

    // İlk açılışta kaldığın sayfaya git
    LaunchedEffect(pageCount) {
        if (pageCount > 0) {
            val target = savedPage.coerceIn(0, pageCount - 1)
            when (mode) {
                ScrollMode.Vertical   -> listState.scrollToItem(target)
                ScrollMode.Horizontal -> pagerState.scrollToPage(target)
            }
            currentPage = target
        }
    }

    // Mod değişince aynı sayfaya hizala
    LaunchedEffect(mode, pageCount, currentPage) {
        if (pageCount > 0) {
            when (mode) {
                ScrollMode.Vertical   -> listState.scrollToItem(currentPage)
                ScrollMode.Horizontal -> pagerState.scrollToPage(currentPage)
            }
        }
    }

    // currentPage akışı
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collectLatest {
            if (mode == ScrollMode.Vertical && it != currentPage) currentPage = it
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collectLatest {
            if (mode == ScrollMode.Horizontal && it != currentPage) currentPage = it
        }
    }

    // Sayfa değiştikçe kaydet + zoom sıfırla
    LaunchedEffect(currentPage, pageCount) {
        if (pageCount > 0) {
            UserPreferencesManager.setLastPage(context, uriString, currentPage)
        }
        resetZoom()
    }

    // Geri tuşu
    BackHandler {
        when {
            scale > 1f -> resetZoom()
            chromeVisible -> chromeVisible = false
            else -> onBack()
        }
    }

    // Cache + preload
    val pageCache = remember { object : LruCache<Int, Bitmap>(6) {} }
    LaunchedEffect(currentPage, renderer, screenPx) {
        val r = renderer ?: return@LaunchedEffect
        preloadWithCache(r, currentPage - 1, screenPx, pageCache)
        preloadWithCache(r, currentPage + 1, screenPx, pageCache)
    }

    // Tek dokunuş davranışı
    fun handleTap(pos: Offset) {
        if (!chromeVisible && mode == ScrollMode.Horizontal && scale <= 1f && displayedSize.width > 0) {
            val leftEdge = displayedSize.width * EDGE_FRACTION
            val rightEdge = displayedSize.width * (1f - EDGE_FRACTION)
            when {
                pos.x <= leftEdge && currentPage > 0 -> {
                    scope.launch { pagerState.animateScrollToPage(currentPage - 1) }
                    return
                }
                pos.x >= rightEdge && currentPage < pageCount - 1 -> {
                    scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                    return
                }
            }
        }
        chromeVisible = !chromeVisible
    }

    // --------- İÇERİK ---------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surface)
    ) {
        if (renderer == null || pageCount == 0) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { refreshTick++ }) { Text("Yeniden dene") }
            }
        } else {
            when (mode) {
                // --- DİKEY ---
                ScrollMode.Vertical -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = scale <= 1f,
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        items(count = pageCount, key = { it }) { index ->
                            var bitmap by remember(index) { mutableStateOf<Bitmap?>(null) }
                            var loadError by remember(index) { mutableStateOf(false) }

                            LaunchedEffect(index, renderer, screenPx, refreshTick) {
                                val r = renderer ?: return@LaunchedEffect
                                loadError = false
                                bitmap = renderWithCache(r, index, screenPx, pageCache)
                                if (bitmap == null) loadError = true
                            }

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    bitmap == null && !loadError -> {
                                        Spacer(Modifier.height(220.dp))
                                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                                    }
                                    loadError -> {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("Sayfa yüklenemedi")
                                            TextButton(onClick = {
                                                pageCache.remove(index)
                                                refreshTick++
                                            }) { Text("Yeniden dene") }
                                        }
                                    }
                                    else -> {
                                        ReaderPageImage(
                                            index = index,
                                            bitmap = bitmap!!,
                                            scale = scale,
                                            offset = offset,
                                            transformState = transformState,
                                            onSizeChanged = { displayedSize = it },
                                            onDoubleTap = { tapPos ->
                                                val newScale = if (scale > 1f) 1f else 2f
                                                if (newScale == 1f) {
                                                    offset = Offset.Zero
                                                } else {
                                                    val dx = (displayedSize.width / 2f - tapPos.x) * (newScale - 1f)
                                                    val dy = (displayedSize.height / 2f - tapPos.y) * (newScale - 1f)
                                                    offset = clampOffset(
                                                        Offset(dx, dy), displayedSize, displayedSize, newScale
                                                    )
                                                }
                                                scale = newScale
                                            },
                                            onSingleTap = { tapPos -> handleTap(tapPos) },
                                            blockParentScrollWhenZoomed = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // --- YATAY ---
                ScrollMode.Horizontal -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = scale <= 1f,
                        pageSpacing = 0.dp,
                        contentPadding = PaddingValues(0.dp)
                    ) { index ->
                        var bitmap by remember(index) { mutableStateOf<Bitmap?>(null) }
                        var loadError by remember(index) { mutableStateOf(false) }

                        LaunchedEffect(index, renderer, screenPx, refreshTick) {
                            val r = renderer ?: return@LaunchedEffect
                            loadError = false
                            bitmap = renderWithCache(r, index, screenPx, pageCache)
                            if (bitmap == null) loadError = true
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                bitmap == null && !loadError -> {
                                    CircularProgressIndicator()
                                }
                                loadError -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Sayfa yüklenemedi")
                                        TextButton(onClick = {
                                            pageCache.remove(index)
                                            refreshTick++
                                        }) { Text("Yeniden dene") }
                                    }
                                }
                                else -> {
                                    ReaderPageImage(
                                        index = index,
                                        bitmap = bitmap!!,
                                        scale = scale,
                                        offset = offset,
                                        transformState = transformState,
                                        onSizeChanged = { displayedSize = it },
                                        onDoubleTap = { tapPos ->
                                            val newScale = if (scale > 1f) 1f else 2f
                                            if (newScale == 1f) {
                                                offset = Offset.Zero
                                            } else {
                                                val dx = (displayedSize.width / 2f - tapPos.x) * (newScale - 1f)
                                                val dy = (displayedSize.height / 2f - tapPos.y) * (newScale - 1f)
                                                offset = clampOffset(
                                                    Offset(dx, dy), displayedSize, displayedSize, newScale
                                                )
                                            }
                                            scale = newScale
                                        },
                                        onSingleTap = { tapPos -> handleTap(tapPos) },
                                        blockParentScrollWhenZoomed = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ÜST ÇUBUK — sadece görünürken
        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TopAppBar(
                title = {
                    Text(displayName ?: "PDF", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (scale > 1f) resetZoom() else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    // Yenile butonu
                    IconButton(onClick = {
                        pageCache.evictAll()
                        refreshTick++
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Yenile")
                    }

                    FilterChip(
                        selected = mode == ScrollMode.Vertical,
                        onClick = { mode = ScrollMode.Vertical },
                        label = { Text("Dikey") },
                        leadingIcon = { Icon(Icons.Filled.SwapVert, contentDescription = null) }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = mode == ScrollMode.Horizontal,
                        onClick = { mode = ScrollMode.Horizontal },
                        label = { Text("Yatay") },
                        leadingIcon = { Icon(Icons.Filled.Swipe, contentDescription = null) }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${(currentPage + 1).coerceAtMost(pageCount)} / $pageCount",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.width(8.dp))
                }
            )
        }

        // ALT YÜZEN ZOOM KONTROLÜ — sadece görünürken
        AnimatedVisibility(
            visible = chromeVisible && pageCount > 0,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                tonalElevation = 3.dp,
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        scale = (scale - 0.25f).coerceAtLeast(1f)
                        if (scale == 1f) offset = Offset.Zero
                    }) { Icon(Icons.Filled.ZoomOut, contentDescription = "Uzaklaştır") }

                    Text("${(scale * 100).toInt()}%")
                    Slider(
                        value = scale,
                        onValueChange = {
                            val s = it.coerceIn(1f, 4f)
                            scale = s
                            if (s == 1f) offset = Offset.Zero
                        },
                        valueRange = 1f..4f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    AssistChip(onClick = { resetZoom() }, label = { Text("100%") })

                    IconButton(onClick = {
                        scale = (scale + 0.25f).coerceAtMost(4f)
                    }) { Icon(Icons.Filled.ZoomIn, contentDescription = "Yakınlaştır") }
                }
            }
        }
    }
}

/* ---------- Page Image (tam ekran) ---------- */

@Composable
private fun ReaderPageImage(
    index: Int,
    bitmap: Bitmap,
    scale: Float,
    offset: Offset,
    transformState: androidx.compose.foundation.gestures.TransformableState,
    onSizeChanged: (IntSize) -> Unit,
    onDoubleTap: (Offset) -> Unit,   // çift dokunma pozisyonu
    onSingleTap: (Offset) -> Unit,   // tek dokunma pozisyonu
    blockParentScrollWhenZoomed: Boolean // API uyumu için bırakıldı
) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Sayfa ${index + 1}",
        contentScale = ContentScale.FillWidth,   // genişliği doldur
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .onSizeChanged { onSizeChanged(it) }
            .transformable(transformState)      // pinch + pan
            .graphicsLayer {
                if (scale > 1f) {
                    scaleX = scale; scaleY = scale
                    translationX = offset.x; translationY = offset.y
                } else {
                    scaleX = 1f; scaleY = 1f
                    translationX = 0f; translationY = 0f
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { pos -> onDoubleTap(pos) },
                    onTap = { pos -> onSingleTap(pos) }
                )
            }
    )
}

/* ---------- Render & Cache helpers (recycle YOK) ---------- */

private suspend fun renderWithCache(
    renderer: PdfRenderer,
    pageIndex: Int,
    targetWidthPx: Int,
    cache: LruCache<Int, Bitmap>
): Bitmap? {
    cache.get(pageIndex)?.let { cached ->
        if (!cached.isRecycled) return cached
        cache.remove(pageIndex)
    }
    val bmp = renderPageBitmapSafe(renderer, pageIndex, targetWidthPx)
    if (bmp != null) cache.put(pageIndex, bmp)
    return bmp
}

private suspend fun preloadWithCache(
    renderer: PdfRenderer,
    pageIndex: Int,
    targetWidthPx: Int,
    cache: LruCache<Int, Bitmap>
) {
    if (pageIndex < 0 || pageIndex >= renderer.pageCount) return
    cache.get(pageIndex)?.let { if (!it.isRecycled) return }
    val bmp = renderPageBitmapSafe(renderer, pageIndex, targetWidthPx)
    if (bmp != null) cache.put(pageIndex, bmp)
}

private suspend fun renderPageBitmapSafe(
    renderer: PdfRenderer,
    pageIndex: Int,
    targetWidthPx: Int
): Bitmap? {
    val candidates = listOf(targetWidthPx, 2048, 1536, 1024, 768)
        .distinct()
        .sortedDescending()
        .filter { it > 0 }
    for (w in candidates) {
        val bmp = tryRender(renderer, pageIndex, w)
        if (bmp != null) return bmp
    }
    return null
}

private suspend fun tryRender(
    renderer: PdfRenderer,
    pageIndex: Int,
    targetWidthPx: Int
): Bitmap? = withContext(Dispatchers.IO) {
    try {
        renderer.openPage(pageIndex).use { page ->
            val ratio = page.height.toFloat() / page.width.toFloat()
            val width = targetWidthPx.coerceAtLeast(1)
            val height = (width * ratio).toInt().coerceAtLeast(1)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            page.render(bmp, Rect(0, 0, width, height), null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bmp
        }
    } catch (_: OutOfMemoryError) { null } catch (_: Throwable) { null }
}

/* ---------- Utility: güvenli display name ---------- */

private fun safeGetDisplayName(cr: ContentResolver, uri: Uri): String? {
    return runCatching {
        var name: String? = null
        var cursor: Cursor? = null
        try {
            cursor = cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = cursor.getString(idx)
            }
        } finally { runCatching { cursor?.close() } }
        if (name.isNullOrBlank()) uri.lastPathSegment else name
    }.getOrNull()
}

/* ---------- Offset clamp helper ---------- */
private fun clampOffset(
    offset: Offset,
    contentSize: IntSize,   // bitmap ekrana sığdırılmış boyut
    containerSize: IntSize, // Image composable boyutu
    scale: Float
): Offset {
    if (scale <= 1f || contentSize.width == 0 || contentSize.height == 0) return Offset.Zero

    val scaledW = contentSize.width * scale
    val scaledH = contentSize.height * scale
    val maxX = ((scaledW - containerSize.width) / 2f).coerceAtLeast(0f)
    val maxY = ((scaledH - containerSize.height) / 2f).coerceAtLeast(0f)

    return Offset(
        x = offset.x.coerceIn(-maxX, maxX),
        y = offset.y.coerceIn(-maxY, maxY)
    )
}
