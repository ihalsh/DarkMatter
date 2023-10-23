package com.github.ihalsh.darkmatter.di.providers

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Pool
import com.github.ihalsh.darkmatter.asset.MusicAsset
import com.github.ihalsh.darkmatter.asset.SoundAsset
import ktx.assets.async.AssetStorage
import ktx.log.logger
import java.util.EnumMap

interface AudioService {
    suspend fun play(musicAsset: MusicAsset, volume: Float = 1f, isLooped: Boolean = true)
    suspend fun stop(clearSounds: Boolean = true, unloadCurrentMusicAsset: Boolean = false)
    fun play(soundAsset: SoundAsset, volume: Float = 1f)
    fun pause()
    fun resume()
    fun update()
}

class AudioServiceImpl (private val assets: AssetStorage) : AudioService {
    private val soundCache = EnumMap<SoundAsset, Sound>(SoundAsset::class.java)
    private val soundRequestPool = SoundRequestPool()
    private val soundRequests = EnumMap<SoundAsset, SoundRequest>(SoundAsset::class.java)
    private var currentMusicAsset: MusicAsset? = null

    override fun play(soundAsset: SoundAsset, volume: Float) {
        when {
            soundAsset in soundRequests -> {
                soundRequests[soundAsset]?.let { request ->
                    request.volume = maxOf(request.volume, volume)
                }
            }

            soundRequests.size >= MAX_SOUND_INSTANCE -> {
                LOG.debug { "Maximum sound request reached" }
                return
            }

            else -> {
                if (soundAsset.descriptor !in assets) {
                    LOG.error { "Trying to play a sound which is not loaded: $soundAsset" }
                    return
                }
                if (soundAsset !in soundCache) {
                    soundCache[soundAsset] = assets[soundAsset.descriptor]
                }
                soundRequests[soundAsset] = soundRequestPool.obtain().apply {
                    this.soundAsset = soundAsset
                    this.volume = volume
                }
            }
        }
    }

    override suspend fun play(musicAsset: MusicAsset, volume: Float, isLooped: Boolean) {
        fun playMusic(asset: MusicAsset, volume: Float, isLooped: Boolean) =
            assets[asset.descriptor].run {
                this.volume = volume
                this.isLooping = isLooped
                play()
            }

        stop(unloadCurrentMusicAsset = true)

        assets.loadAsync(musicAsset.descriptor).join()

        if (assets.isLoaded(musicAsset.descriptor)) {
            currentMusicAsset = musicAsset
            playMusic(musicAsset, volume, isLooped)
        }
    }

    override fun pause() {
        currentMusicAsset?.let {
            assets[it.descriptor].pause()
        }
    }

    override fun resume() {
        currentMusicAsset?.let {
            assets[it.descriptor].play()
        }
    }

    override suspend fun stop(clearSounds: Boolean, unloadCurrentMusicAsset: Boolean) {
        currentMusicAsset?.let {
            assets[it.descriptor].stop()
            if (clearSounds) soundRequests.clear()
            if (unloadCurrentMusicAsset) assets.unload(it.descriptor)
        }
    }

    override fun update() {
        if (!soundRequests.isEmpty()) {
            soundRequests.values.forEach { request ->
                soundCache[request.soundAsset]?.play(request.volume)
                soundRequestPool.free(request)
            }
            soundRequests.clear()
        }
    }

    private class SoundRequest : Pool.Poolable {
        lateinit var soundAsset: SoundAsset
        var volume = 1f

        override fun reset() {
            volume = 1f
        }
    }

    private class SoundRequestPool : Pool<SoundRequest>() {
        override fun newObject(): SoundRequest = SoundRequest()
    }

    companion object {
        private val LOG = logger<AudioService>()

        private const val MAX_SOUND_INSTANCE = 16f
    }
}
