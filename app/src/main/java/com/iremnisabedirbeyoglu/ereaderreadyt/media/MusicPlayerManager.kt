package com.iremnisabedirbeyoglu.ereaderreadyt.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Güvenli, sızıntısız ve çökmeden çalışan basit müzik yöneticisi.
 * - MediaPlayer.create() kullanmak yerine manuel init + prepareAsync kullanır.
 *   (AudioAttributes'ı prepare'dan ÖNCE ayarlamak için gereklidir.)
 * - applicationContext kullanır → Activity sızıntısı olmaz.
 * - Hata durumlarında sessizce toparlar; çökmez.
 * - isPlaying akışı UI'da gözlemlenebilir.
 */
object MusicPlayerManager {
    private var player: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    /**
     * Arka plan müziğini başlatır (loop:true).
     * Aynı anda tek MediaPlayer tutulur; yeni çağrıda eskisi kapatılıp yenisi kurulur.
     */
    fun play(context: Context, resId: Int) {
        if (resId == 0) return // geçersiz kaynak ID'si koruması

        val app = context.applicationContext
        // Önce var olanı temizle
        stopInternal(release = true)

        val mp = MediaPlayer()
        player = mp
        try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            // Raw kaynağını güvenli şekilde bağla
            val afd = app.resources.openRawResourceFd(resId) ?: run {
                stopInternal(release = true)
                return
            }
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            mp.isLooping = true

            mp.setOnPreparedListener {
                it.start()
                _isPlaying.value = true
            }
            mp.setOnCompletionListener {
                // looping false olsaydı buraya düşerdi
                _isPlaying.value = false
            }
            mp.setOnErrorListener { p, _, _ ->
                // herhangi bir hata durumunda güvenli kapat
                runCatching { p.reset() }
                runCatching { p.release() }
                player = null
                _isPlaying.value = false
                true
            }

            mp.prepareAsync()
        } catch (_: Exception) {
            // Kurulumda hata olursa güvenli kapat
            stopInternal(release = true)
        }
    }

    /** Çalan müziği durdurur ve kaynağı SERBEST bırakır. */
    fun stop() = stopInternal(release = true)

    /** Çalan müziği sadece duraklatır (resume ile kaldığı yerden devam eder). */
    fun pause() {
        val p = player ?: return
        if (p.isPlaying) {
            runCatching { p.pause() }
            _isPlaying.value = false
        }
    }

    /** Duraklatılmış müziği devam ettirir. */
    fun resume() {
        val p = player ?: return
        if (!p.isPlaying) {
            runCatching { p.start() }
            _isPlaying.value = true
        }
    }

    /** Basit toggle: çalıyorsa durdurur, duruyorsa başlatır. */
    fun toggle(context: Context, resId: Int) {
        if (_isPlaying.value) stop() else play(context, resId)
    }

    // ---- internal ----
    private fun stopInternal(release: Boolean) {
        val p = player ?: return run { _isPlaying.value = false }
        runCatching { if (p.isPlaying) p.stop() }
        if (release) {
            runCatching { p.reset() }
            runCatching { p.release() }
            player = null
        }
        _isPlaying.value = false
    }
}
