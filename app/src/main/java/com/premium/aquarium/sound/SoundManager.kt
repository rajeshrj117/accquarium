package com.premium.aquarium.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

/**
 * Sound manager. Loads optional raw resources if present under res/raw/
 * (bubble.mp3, feed.mp3, eat.mp3, chime.mp3, splash.mp3, water_ambient.mp3).
 * If those files are not added to res/raw, this class still compiles and
 * simply plays nothing (soundId stays 0, play() calls are skipped).
 *
 * To enable real audio: drop royalty-free mp3/ogg files into
 * app/src/main/res/raw/ with the exact names above, then uncomment the
 * R.raw references in initSoundPool()/initAmbient().
 */
class SoundManager(private val context: Context) {

    private var ambientPlayer: MediaPlayer? = null
    private lateinit var soundPool: SoundPool

    private var bubbleSoundId = 0
    private var feedSoundId = 0
    private var eatSoundId = 0
    private var chimeSoundId = 0
    private var splashSoundId = 0

    private var isMuted = false

    init {
        initSoundPool()
        initAmbient()
    }

    private fun initSoundPool() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(attrs)
            .build()

        // Uncomment once you've added res/raw/*.mp3 files:
        // bubbleSoundId = soundPool.load(context, R.raw.bubble, 1)
        // feedSoundId = soundPool.load(context, R.raw.feed, 1)
        // eatSoundId = soundPool.load(context, R.raw.eat, 1)
        // chimeSoundId = soundPool.load(context, R.raw.chime, 1)
        // splashSoundId = soundPool.load(context, R.raw.splash, 1)
    }

    private fun initAmbient() {
        try {
            ambientPlayer = MediaPlayer().apply {
                isLooping = true
                setVolume(0.3f, 0.3f)
                // Uncomment once you've added res/raw/water_ambient.mp3:
                // setDataSource(context, Uri.parse("android.resource://${context.packageName}/${R.raw.water_ambient}"))
                // prepare()
                // start()
            }
        } catch (e: Exception) {
            // Graceful fallback - ambient sound is optional
        }
    }

    fun playBubble() = play(bubbleSoundId, 0.4f)
    fun playFeed() = play(feedSoundId, 0.7f)
    fun playEat() = play(eatSoundId, 0.8f)
    fun playChime() = play(chimeSoundId, 0.6f)
    fun playSplash() = play(splashSoundId, 0.9f)

    private fun play(soundId: Int, volume: Float) {
        if (isMuted || soundId == 0) return
        soundPool.play(soundId, volume, volume, 1, 0, 1f)
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        ambientPlayer?.setVolume(if (muted) 0f else 0.3f, if (muted) 0f else 0.3f)
    }

    fun release() {
        ambientPlayer?.release()
        soundPool.release()
    }
}
