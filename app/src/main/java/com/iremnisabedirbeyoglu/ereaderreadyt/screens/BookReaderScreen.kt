package com.iremnisabedirbeyoglu.ereaderreadyt.screens

import android.graphics.Bitmap
import android.graphics.Color.parseColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iremnisabedirbeyoglu.ereaderreadyt.data.UserPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import getDisplayName
import com.iremnisabedirbeyoglu.ereaderreadyt.data.PdfStorageManager // NEW


private enum class ScrollMode { Vertical, Horizontal }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReaderScreen(
    uriString: String?,
    onBack: () -> Unit,                 // 🔙 dışarıdan geri davranışı
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (uriString == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("PDF bulunamadı.") }
        return
    }
    val uri = remember(uriString) { Uri.parse(uriString) }

    // Arka plan
    val bgHex by UserPreferencesManager.getBackgroundColor(context).collectAsState(initial = "#FAF8F6")
    val surface = remember(bgHex) { Color(parseColor(bgHex)) }

    // PdfRenderer
    var pageCount by remember { mutableStateOf(0) }
    var displayName by remember { mutableStateOf<String?>(null) }
    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pfd by remember { mutableStateOf<ParcelFileDescriptor?>(null) }

    // Kaldığın yerden devam
    val savedPage by UserPreferencesManager.getLastPage(context, uriString).collectAsState(initial = 0)

    LaunchedEffect(uri) {
        displayName = getDisplayName(context.contentResolver, uri)
        context.contentResolver.openFileDescriptor(uri, "r")?.let { fd ->
            pfd = fd
            renderer = PdfRenderer(fd)
            pageCount = renderer?.pageCount ?: 0
        }
        // NEW: Son okunan kitabı kaydet
        PdfStorageManager.setLastReadPdf(context, uri)
    }
    DisposableEffect(uri) {
        onDispose {
            try { renderer?.close() } catch (_: Throwable) {}
            try { pfd?.close() } catch (_: Throwable) {}
        }
    }

    // Okuma modu
    var mode by rememberSaveable { mutableStateOf(ScrollMode.Vertical) }

    // Sayfa takibi
    val listState = rememberLazyListState()
    val pagerState = rememberPagerState(pageCount = { pageCount })
    var currentPage by rememberSaveable { mutableStateOf(0) }

    // İlk açılışta savedPage'e atla
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

    // Mode değişince o anki sayfaya senkron kaydır ✅
    LaunchedEffect(mode, pageCount, currentPage) {
        if (pageCount > 0) {
            when (mode) {
                ScrollMode.Vertical   -> listState.scrollToItem(currentPage)
                ScrollMode.Horizontal -> pagerState.scrollToPage(currentPage)
            }
        }
    }

    // Liste/pager akışından currentPage'i güncel tut
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect {
            if (mode == ScrollMode.Vertical) currentPage = it
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            if (mode == ScrollMode.Horizontal) currentPage = it
        }
    }

    // her sayfa değişiminde kaydet
    LaunchedEffect(currentPage, pageCount) {
        if (pageCount > 0) {
            UserPreferencesManager.setLastPage(context, uriString, currentPage)
        }
    }

    // Zoom & pan + geri/zoom davranışı
    var scale by rememberSaveable { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 4f)
        if (newScale > 1f) offset += panChange else offset = Offset.Zero
        scale = newScale
    }
    fun resetZoom() { scale = 1f; offset = Offset.Zero }

    // Sistem geri tuşu: önce zoom’u sıfırla, sonra çık
    BackHandler {
        if (scale > 1f) resetZoom() else onBack()
    }

    // ---- Basit sayfa önbelleği (±1 sayfa) ----
    // Giriş kapasitesi: 6 bmp (küçük ama etkili). LruCache default size = entry sayısı.
    val pageCache = remember {
        object : LruCache<Int, Bitmap>(6) {
            override fun entryRemoved(evicted: Boolean, key: Int?, oldValue: Bitmap?, newValue: Bitmap?) {
                // İstenirse aggressive cleanup yapılabilir; şimdilik recycle etmiyoruz.
            }
        }
    }

    // Ekran genişliği (px) — render çözünürlüğü için
    val screenPx = with(LocalConfiguration.current) {
        (screenWidthDp * context.resources.displayMetrics.density).toInt()
    }

    // Geçerli sayfa değiştikçe komşu sayfaları önden hazırla ✅
    LaunchedEffect(currentPage, renderer, screenPx) {
        val r = renderer ?: return@LaunchedEffect
        preloadWithCache(r, currentPage - 1, screenPx, pageCache)
        preloadWithCache(r, currentPage + 1, screenPx, pageCache)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(displayName ?: "PDF", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (scale > 1f) resetZoom() else onBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri") }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("${(currentPage + 1).coerceAtMost(pageCount)} / $pageCount",
                        style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(8.dp))
                }
            )
        },
        bottomBar = {
            if (pageCount > 0) {
                ZoomBar(
                    scale = scale,
                    onScaleChange = { s -> scale = s; if (s == 1f) offset = Offset.Zero },
                    onResetZoom = { resetZoom() },
                    onZoomIn = { scale = (scale + 0.25f).coerceAtMost(4f) },
                    onZoomOut = { scale = (scale - 0.25f).coerceAtLeast(1f).also { if (it == 1f) offset = Offset.Zero } }
                )
            }
        },
        containerColor = surface
    ) { inner ->
        if (renderer == null || pageCount == 0) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            when (mode) {
                ScrollMode.Vertical -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(inner),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp, start = 8.dp, end = 8.dp, top = 8.dp)
                    ) {
                        items((0 until pageCount).toList(), key = { it }) { index ->
                            var bitmap by remember(index) { mutableStateOf<Bitmap?>(null) }
                            var loadError by remember(index) { mutableStateOf(false) }

                            LaunchedEffect(index, renderer, screenPx) {
                                loadError = false
                                bitmap = renderWithCache(renderer!!, index, screenPx, pageCache)
                                if (bitmap == null) loadError = true
                            }

                            PageCard {
                                if (bitmap == null && !loadError) {
                                    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                } else if (loadError) {
                                    ErrorRetry(onRetry = {
                                        scope.launch {
                                            loadError = false
                                            bitmap = renderWithCache(renderer!!, index, screenPx, pageCache)
                                            if (bitmap == null) loadError = true
                                        }
                                    })
                                } else {
                                    PageImage(
                                        index = index,
                                        bitmap = bitmap,
                                        scale = scale,
                                        offset = offset,
                                        enableTransform = scale > 1f,
                                        transformState = transformState,
                                        onDoubleTapToggle = { if (scale > 1f) resetZoom() else scale = 2f }
                                    )
                                }
                            }
                        }
                    }
                }
                ScrollMode.Horizontal -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize().padding(inner),
                        pageSpacing = 12.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) { index ->
                        var bitmap by remember(index) { mutableStateOf<Bitmap?>(null) }
                        var loadError by remember(index) { mutableStateOf(false) }

                        LaunchedEffect(index, renderer, screenPx) {
                            loadError = false
                            bitmap = renderWithCache(renderer!!, index, screenPx, pageCache)
                            if (bitmap == null) loadError = true
                        }

                        PageCard {
                            if (bitmap == null && !loadError) {
                                Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            } else if (loadError) {
                                ErrorRetry(onRetry = {
                                    scope.launch {
                                        loadError = false
                                        bitmap = renderWithCache(renderer!!, index, screenPx, pageCache)
                                        if (bitmap == null) loadError = true
                                    }
                                })
                            } else {
                                PageImage(
                                    index = index,
                                    bitmap = bitmap,
                                    scale = scale,
                                    offset = offset,
                                    enableTransform = scale > 1f,
                                    transformState = transformState,
                                    onDoubleTapToggle = { if (scale > 1f) resetZoom() else scale = 2f }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------- UI parçaları ---------- */

@Composable
private fun ErrorRetry(onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().height(220.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sayfa yüklenemedi.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRetry) { Text("Tekrar Dene") }
    }
}

@Composable
private fun ZoomBar(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    onResetZoom: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit
) {
    Surface(tonalElevation = 2.dp) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${(scale * 100).toInt()}%")
                Slider(
                    value = scale,
                    onValueChange = onScaleChange,
                    valueRange = 1f..4f,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                AssistChip(onClick = onResetZoom, label = { Text("100%") })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onZoomOut) { Icon(Icons.Filled.ZoomOut, "Uzaklaştır") }
                IconButton(onClick = onZoomIn) { Icon(Icons.Filled.ZoomIn, "Yakınlaştır") }
            }
        }
    }
}

@Composable
private fun PageCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) { Box(Modifier.padding(6.dp)) { content() } }
}

@Composable
private fun PageImage(
    index: Int,
    bitmap: Bitmap?,
    scale: Float,
    offset: Offset,
    enableTransform: Boolean,
    transformState: androidx.compose.foundation.gestures.TransformableState,
    onDoubleTapToggle: () -> Unit
) {
    Image(
        bitmap = bitmap!!.asImageBitmap(),
        contentDescription = "Sayfa ${index + 1}",
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .graphicsLayer {
                if (scale > 1f) {
                    scaleX = scale; scaleY = scale
                    translationX = offset.x; translationY = offset.y
                } else {
                    scaleX = 1f; scaleY = 1f; translationX = 0f; translationY = 0f
                }
            }
            .then(if (enableTransform) Modifier.transformable(transformState) else Modifier)
            .pointerInput(scale) { detectTapGestures(onDoubleTap = { onDoubleTapToggle() }) },
        contentScale = ContentScale.FillWidth
    )
}

/* ---------- Render & Cache helpers ---------- */

/** Cache'e bakarak güvenli render yapar. */
private suspend fun renderWithCache(
    renderer: PdfRenderer,
    pageIndex: Int,
    targetWidthPx: Int,
    cache: LruCache<Int, Bitmap>
): Bitmap? {
    cache.get(pageIndex)?.let { return it }
    val bmp = renderPageBitmapSafe(renderer, pageIndex, targetWidthPx)
    if (bmp != null) cache.put(pageIndex, bmp)
    return bmp
}

/** Komşu sayfayı önceden hazırla (cache'e koy). */
private suspend fun preloadWithCache(
    renderer: PdfRenderer,
    pageIndex: Int,
    targetWidthPx: Int,
    cache: LruCache<Int, Bitmap>
) {
    if (pageIndex < 0 || pageIndex >= renderer.pageCount) return
    if (cache.get(pageIndex) != null) return
    val bmp = renderPageBitmapSafe(renderer, pageIndex, targetWidthPx)
    if (bmp != null) cache.put(pageIndex, bmp)
}

/** OOM veya render hatasında daha küçük genişlikle tekrar dener. */
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
            val height = (width * ratio).toInt()
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bmp
        }
    } catch (_: OutOfMemoryError) {
        null
    } catch (_: Throwable) {
        null
    }
}







