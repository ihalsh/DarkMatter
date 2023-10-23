package com.github.ihalsh.darkmatter.screen

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Preferences
import com.github.ihalsh.darkmatter.DarkMatter
import com.github.ihalsh.darkmatter.asset.I18NBundleAsset
import com.github.ihalsh.darkmatter.asset.MusicAsset
import com.github.ihalsh.darkmatter.di.providers.AudioService
import com.github.ihalsh.darkmatter.di.providers.EventDispatcher
import com.github.ihalsh.darkmatter.di.providers.ViewportProvider
import com.github.ihalsh.darkmatter.event.GameEvent.GameOver
import com.github.ihalsh.darkmatter.event.GameEvent.PlayerMove
import com.github.ihalsh.darkmatter.event.GameEvent.PlayerSpawn
import com.github.ihalsh.darkmatter.event.GameEvent.PowerUpCollected
import com.github.ihalsh.darkmatter.event.GameEvent.RestartGame
import com.github.ihalsh.darkmatter.event.GameEvent.ShipDamaged
import com.github.ihalsh.darkmatter.ui.GameOverUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import ktx.assets.async.AssetStorage
import ktx.async.RenderingScope
import ktx.inject.Context
import ktx.log.logger
import ktx.preferences.get
import kotlin.math.roundToInt

class GameOverScreen(context: Context) : KtxScreen {
    private val game = context.inject<DarkMatter>()
    private val eventDispatcher = context.inject<EventDispatcher>()
    private val engine = context.inject<PooledEngine>()
    private val stage = context.inject<ViewportProvider>().stage
    private val bundle = context.inject<AssetStorage>()[I18NBundleAsset.DEFAULT.descriptor]
    private val audioService = context.inject<AudioService>()
    private val preferences = context.inject<Preferences>()
    private lateinit var scope: CoroutineScope


    override fun show() {
        scope = RenderingScope()
        startMusic()
        bindGameEventListeners()
    }

    override fun hide() {
        LOG.debug { "Hide ${this::class.simpleName}" }
        scope.cancel()
    }

    override fun render(delta: Float) {
        engine.update(delta)
        stage.run {
            viewport.apply()
            act(delta)
            draw()
        }
    }

    private fun bindGameEventListeners() {
        scope.launch {
            eventDispatcher.events.collect { event ->
                LOG.debug { "$event" }
                when (event) {
                    is GameOver -> {
                        setupUi(
                            score = event.distance.roundToInt(),
                            highScore = preferences[GameScreen.PREFERENCE_HIGHSCORE_KEY, 0f].toInt()
                        )
                    }

                    is RestartGame -> {
                        stage.clear()
                        LOG.debug { "Number of entities: ${engine.entities.size()}" }
                        engine.removeAllEntities()
                        audioService.stop()
                        game.setScreen<GameScreen>()
                    }

                    is PowerUpCollected, is ShipDamaged,
                    is PlayerMove, is PlayerSpawn -> {
                        /* Do nothing for now */
                    }
                }
            }
        }
    }

    private fun setupUi(score: Int, highScore: Int) = GameOverUI(bundle).apply {
        restartButton.onClick {
            eventDispatcher.dispatchEvent(RestartGame::class)
        }
        updateScores(score = score, highScore = highScore)
        stage += this.table
    }

    private fun startMusic() {
        scope.launch { audioService.play(MusicAsset.GAME_OVER, volume = 0.125f) }
    }

    companion object {
        private val LOG = logger<GameOverScreen>()
    }
}
