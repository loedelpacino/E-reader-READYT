// path: app/src/main/java/com/iremnisabedirbeyoglu/ereaderreadyt/util/PageSfx.kt
package com.iremnisabedirbeyoglu.ereaderreadyt.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Sayfa çevirme SFX yöneticisi.
 *
 * Özellikler:
 * - Güvenli init/release, applicationContext kullanır (sızıntı yok).
 * - Kaynak adı/ID bulunamazsa no-op (çökmez).
 * - Yükleme tamamlanmadan play çağrılırsa kısa kuyrukta bekletip hazır olunca çalar.
 * - Etkin/pasif (mute) kontrolü.
 */
object PageSfx {
    @Volatile private var soundPool: SoundPool? = null
    @Volatile private var soundId: Int = 0
    @Volatile private var isLoaded: Boolean = false
    @Volatile private var enabled: Boolean = true
    @Volatile private var pendingPlays: Int = 0 // load tamamlanana kadar bekleyen max 2 çalma

    private val defaultNames = listOf("page_flip", "pageflip", "flip", "paper_flip")

    /**
     * SFX’i başlatır. İstersen doğrudan bir raw resId verebilirsin, yoksa verilen isimleri dener.
     * @return Hazırsa true, aksi halde false (no-op davranır).
     */
    @Synchronized
    fun init(
        context: Context,
        resId: Int? = null,
        candidateNames: List<String> = defaultNames
    ): Boolean {
        // Zaten kuruluysa, mevcut durumu dön.
        soundPool?.let { return isLoaded }

        val app = context.applicationContext
        val sp = SoundPool.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setMaxStreams(2)
            .build()

        soundPool = sp
        isLoaded = false
        pendingPlays = 0

        val idToLoad = resId ?: findFirstExistingRaw(app, candidateNames)
        if (idToLoad == 0) {
            // Kaynak yoksa sessiz no-op; havuz yine de serbest bırakılır.
            release()
            return false
        }

        sp.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId == soundId) {
                isLoaded = true
                // Yükleme tamamlandıktan sonra bekleyen çalmaları tetikle
                val toPlay = pendingPlays
                pendingPlays = 0
                repeat(toPlay.coerceAtMost(2)) { playInternal(1f, 1f, 1f) }
            }
        }

        soundId = sp.load(app, idToLoad, 1)
        return true
    }

    /** Etkinliği (mute) ayarla. false → çalma çağrıları no-op olur. */
    fun setEnabled(value: Boolean) { enabled = value }

    /** Hazır mı (yüklendi mi)? */
    fun isReady(): Boolean = isLoaded

    /** Basit oynatım. rate: 0.5f..2.0f */
    fun playFlip(rate: Float = 1f, volume: Float = 1f) {
        val sp = soundPool ?: return
        if (!enabled) return
        if (!isLoaded) { pendingPlays = (pendingPlays + 1).coerceAtMost(2); return }
        playInternal(volume, volume, rate)
    }

    /** Kaynakları serbest bırak. */
    @Synchronized
    fun release() {
        runCatching { soundPool?.release() }
        soundPool = null
        soundId = 0
        isLoaded = false
        pendingPlays = 0
    }

    // --- internal ---

    private fun playInternal(leftVol: Float, rightVol: Float, rate: Float) {
        val sp = soundPool ?: return
        val id = soundId
        if (id == 0) return
        sp.play(
            id,
            leftVol.coerceIn(0f, 1f),
            rightVol.coerceIn(0f, 1f),
            /* priority = */ 1,
            /* loop = */ 0,
            rate.coerceIn(0.5f, 2f)
        )
    }

    private fun findFirstExistingRaw(context: Context, names: List<String>): Int {
        val res = context.resources
        val pkg = context.packageName
        for (name in names) {
            val id = res.getIdentifier(name, "raw", pkg)
            if (id != 0) return id
        }
        return 0
    }
}
