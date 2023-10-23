package com.github.ihalsh.darkmatter.screen

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.github.ihalsh.darkmatter.DarkMatter
import com.github.ihalsh.darkmatter.asset.I18NBundleAsset
import com.github.ihalsh.darkmatter.asset.MusicAsset
import com.github.ihalsh.darkmatter.di.providers.AudioService
import com.github.ihalsh.darkmatter.di.providers.EventDispatcher
import com.github.ihalsh.darkmatter.di.providers.ViewportProvider
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent.Companion.MAX_LIFE
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent.Companion.MAX_SHIELD
import com.github.ihalsh.darkmatter.ecs.component.PowerUpComponent
import com.github.ihalsh.darkmatter.ecs.system.MoveSystem
import com.github.ihalsh.darkmatter.ecs.system.PlayerAnimationSystem
import com.github.ihalsh.darkmatter.ecs.system.PowerUpSystem
import com.github.ihalsh.darkmatter.ecs.system.RenderSystem
import com.github.ihalsh.darkmatter.event.GameEvent
import com.github.ihalsh.darkmatter.event.GameEvent.GameOver
import com.github.ihalsh.darkmatter.event.GameEvent.PlayerMove
import com.github.ihalsh.darkmatter.event.GameEvent.PlayerSpawn
import com.github.ihalsh.darkmatter.event.GameEvent.PowerUpCollected
import com.github.ihalsh.darkmatter.event.GameEvent.RestartGame
import com.github.ihalsh.darkmatter.event.GameEvent.ShipDamaged
import com.github.ihalsh.darkmatter.ui.GameUI
import com.github.ihalsh.darkmatter.utils.PLAYER_START_SPEED
import com.github.ihalsh.darkmatter.utils.addDarkMatter
import com.github.ihalsh.darkmatter.utils.addPlayerShip
import com.github.ihalsh.darkmatter.utils.playerComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.actors.onChangeEvent
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import ktx.ashley.getSystem
import ktx.assets.async.AssetStorage
import ktx.async.RenderingScope
import ktx.inject.Context
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import java.lang.Float.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

class GameScreen(context: Context) : KtxScreen {
    private val game = context.inject<DarkMatter>()
    private val assets = context.inject<AssetStorage>()
    private val eventDispatcher = context.inject<EventDispatcher>()
    private val engine = context.inject<PooledEngine>()
    private val gameViewport = context.inject<ViewportProvider>().gameViewport
    private val uiViewport = context.inject<ViewportProvider>().uiViewport
    private val stage = context.inject<ViewportProvider>().stage
    private val audioService = context.inject<AudioService>()
    private val preferences = context.inject<Preferences>()
    private val bundle = assets[I18NBundleAsset.DEFAULT.descriptor]
    private val renderSystem = engine.getSystem<RenderSystem>()
    private val ui = GameUI(bundle)
    private var isPaused = true
    private lateinit var scope: CoroutineScope

    init {
        engine.addDarkMatter()
    }

    override fun show() {
        LOG.debug { "GameScreen is shown, distance: ${preferences[PREFERENCE_HIGHSCORE_KEY, 0f]}" }
        isPaused = true
        scope = RenderingScope()
        scope.launch { audioService.play(MusicAsset.GAME, volume = 0.125f) }
        bindGameEventListeners()

        engine.run {
            getSystem<PowerUpSystem>().setProcessing(true)
            getSystem<MoveSystem>().setProcessing(true)
            getSystem<PlayerAnimationSystem>().setProcessing(true)
        }

        setupUI()
    }

    override fun resize(width: Int, height: Int) {
        uiViewport.update(width, height, true)
        gameViewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        if (Gdx.input.justTouched() && ui.touchToBeginLabel.isVisible) {
            isPaused = false
            ui.touchToBeginLabel.isVisible = false
            ui.pauseResumeButton.touchable = Touchable.enabled
            ui.quitImageButton.touchable = Touchable.enabled
            eventDispatcher.dispatchEvent(PlayerSpawn::class)
        }

        // More info here: https://gafferongames.com/post/fix_your_timestep/
        val deltaTime = min(delta, MAX_DELTA_TIME)
        if (isPaused) renderSystem.update(0f) else engine.update(deltaTime)

        // render UI
        stage.run {
            viewport.apply()
            act(deltaTime)
            draw()
        }
    }

    override fun hide() {
        super.hide()
        engine.run {
            // remove any power ups and reset the spawn timer
            getSystem<PowerUpSystem>().run {
                setProcessing(false)
                this.reset()
            }
            getSystem<MoveSystem>().setProcessing(false)
            getSystem<PlayerAnimationSystem>().setProcessing(false)
        }
        scope.cancel()
    }

    private fun setupUI() {
        val confirmDialog = ConfirmDialog(bundle).apply {
            yesButton.onClick { Gdx.app.exit() }
            noButton.onClick {
                audioService.resume()
                isPaused = false
                hide()
            }
        }

        ui.run {
            // reset to initial values
            updateDistance(0f)
            updateSpeed(PLAYER_START_SPEED)
            updateLife(MAX_LIFE, MAX_LIFE)
            updateShield(0f, MAX_SHIELD)

            // disable pauseResume button until game was started
            pauseResumeButton.run {
                this.touchable = Touchable.disabled
                this.isChecked = false

                onChangeEvent {
                    isPaused = isChecked
                    when (this.isChecked) {
                        true -> audioService.pause()
                        else -> audioService.resume()
                    }
                }
            }

            // disable quitImage button until game was started
            quitImageButton.run {
                touchable = Touchable.disabled

                onClick {
                    isPaused = true
                    audioService.pause()
                    confirmDialog.show(stage)
                }
            }

            touchToBeginLabel.isVisible = true
        }
        stage += ui
    }

    private fun bindGameEventListeners() {
        scope.launch {
            eventDispatcher.events.collect { event ->
                LOG.debug { "$event" }
                when (event) {
                    is RestartGame -> {
                        engine.addDarkMatter()
                        ui.updateDistance(0f)
                    }

                    is PlayerSpawn -> {
                        LOG.debug { "Spawn new player" }
                        engine.addPlayerShip()
                    }

                    is GameOver -> {
                        val distance = event.distance.roundToInt()
                        LOG.debug { "Player died with a distance of $distance" }
                        preferences[PREFERENCE_HIGHSCORE_KEY, 0f].let { highScore ->
                            preferences.flush {
                                this[PREFERENCE_HIGHSCORE_KEY] = max(distance.toFloat(), highScore)
                            }
                        }

                        launch {
                            delay(2.seconds)
                            stage.clear()
                            LOG.debug { "Number of entities: ${engine.entities.size()}" }
                            engine.removeAllEntities()
                            audioService.stop()
                            game.setScreen<GameOverScreen>()
                        }
                    }

                    is PlayerMove -> {
                        ui.run {
                            updateDistance(event.distance)
                            updateSpeed(event.speed)
                        }
                    }

                    is PowerUpCollected -> {
                        if (event.type == PowerUpComponent.PowerUpType.LIFE) {
                            val player = event.player.playerComponent
                            ui.updateLife(player.life, player.maxLife)
                        }
                    }

                    is ShipDamaged -> {
                        ui.run {
                            updateLife(event.life, event.maxLife)
                            showWarning()
                        }
                    }
                }

                playSoundEffect(event)
            }
        }
    }

    private fun playSoundEffect(event: GameEvent) {
        event.soundAsset?.let {
            with(audioService) {
                play(it)
                update()
            }
        }
    }

    companion object {
        private val LOG = logger<GameScreen>()

        private const val MAX_DELTA_TIME = 1 / 20f

        // Preferences
        const val PREFERENCE_HIGHSCORE_KEY = "preference_highscore_key"
    }
}
