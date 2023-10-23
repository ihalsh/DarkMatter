package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils.clamp
import com.badlogic.gdx.math.Vector2
import com.github.ihalsh.darkmatter.asset.ShaderProgramAsset
import com.github.ihalsh.darkmatter.asset.TextureAsset
import com.github.ihalsh.darkmatter.di.providers.EventDispatcher
import com.github.ihalsh.darkmatter.di.providers.ViewportProvider
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import com.github.ihalsh.darkmatter.ecs.component.PowerUpComponent.PowerUpType.SPEED_1
import com.github.ihalsh.darkmatter.ecs.component.PowerUpComponent.PowerUpType.SPEED_2
import com.github.ihalsh.darkmatter.ecs.component.RemoveComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import com.github.ihalsh.darkmatter.event.GameEvent.GameOver
import com.github.ihalsh.darkmatter.event.GameEvent.PlayerMove
import com.github.ihalsh.darkmatter.event.GameEvent.PlayerSpawn
import com.github.ihalsh.darkmatter.event.GameEvent.PowerUpCollected
import com.github.ihalsh.darkmatter.event.GameEvent.RestartGame
import com.github.ihalsh.darkmatter.event.GameEvent.ShipDamaged
import com.github.ihalsh.darkmatter.utils.graphicsComponent
import com.github.ihalsh.darkmatter.utils.playerComponent
import com.github.ihalsh.darkmatter.utils.transformComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.assets.async.AssetStorage
import ktx.async.RenderingScope
import ktx.graphics.use
import ktx.inject.Context
import ktx.log.logger
import kotlin.math.min

class RenderSystem(context: Context) : SortedIteratingSystem(
    allOf(TransformComponent::class, GraphicComponent::class).get(),
    compareBy { entity -> entity[TransformComponent.mapper] }
), CoroutineScope by RenderingScope() {
    private val eventDispatcher = context.inject<EventDispatcher>()
    private val batch = context.inject<SpriteBatch>()
    private val gameViewport = context.inject<ViewportProvider>().gameViewport
    private val uiViewport = context.inject<ViewportProvider>().uiViewport
    private val backgroundTexture = context.inject<AssetStorage>()[TextureAsset.BACKGROUND.descriptor]
    private val background = Sprite(backgroundTexture.apply { setWrap(Repeat, Repeat) })
    private val backgroundScrollSpeed = Vector2(SCROLL_SPEED_HORIZONTAL, SCROLL_SPEED_VERTICAL)
    private val outlineShader = context.inject<AssetStorage>()[ShaderProgramAsset.OUTLINE.descriptor]
    private val colorTintShader = context.inject<AssetStorage>()[ShaderProgramAsset.TINT.descriptor]


    private val playerEntities by lazy {
        engine.getEntitiesFor(allOf(PlayerComponent::class).exclude(RemoveComponent::class.java).get())
    }

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        bindGameEventListeners()
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        cancel()
    }

    override fun update(deltaTime: Float) {
        // render background
        uiViewport.apply()
        batch.use(uiViewport.camera.combined) {
            background.run {
                // gradually restoring scrolling speed
                backgroundScrollSpeed.y = min(
                    SCROLL_SPEED_VERTICAL,
                    backgroundScrollSpeed.y + deltaTime / 10f
                )

                scroll(backgroundScrollSpeed.x * deltaTime, backgroundScrollSpeed.y * deltaTime)
                draw(it)
            }
        }

        forceSort()
        gameViewport.apply()
        batch.use(gameViewport.camera.combined) { batch ->
            super.update(deltaTime)
            playerEntities.forEach { player ->
                applyOutlineShader(batch, player)
                applyColorTintShader(batch, player)
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity.transformComponent
        val graphics = entity.graphicsComponent

        if (graphics.sprite.texture == null) {
            LOG.error { "Entity |entity| has no texture in its GraphicComponent. entity=$entity" }
            return
        }

        graphics.sprite.run {
            rotation = transform.rotationDeg
            setBounds(
                transform.interpolatedPosition.x,
                transform.interpolatedPosition.y,
                transform.size.x,
                transform.size.y
            )
            draw(batch)
        }
    }

    private fun bindGameEventListeners() {
        launch {
            eventDispatcher.events.collect { event ->
                when (event) {
                    is PowerUpCollected -> {
                        backgroundScrollSpeed.y += when (event.type) {
                            SPEED_1 -> SCROLL_SPEED_VERTICAL
                            SPEED_2 -> SCROLL_SPEED_VERTICAL * 2
                            else -> 0f
                        }
                    }

                    is GameOver, is ShipDamaged,
                    is RestartGame, is PlayerMove, is PlayerSpawn -> {
                        /* Do nothing for now */
                    }
                }
            }
        }
    }

    private fun applyOutlineShader(batch: SpriteBatch, entity: Entity) {
        val player = entity.playerComponent
        val graphics = entity.graphicsComponent

        batch.shader = outlineShader
        OUTLINE_COLOR.a = clamp(player.shield / player.maxShield, 0f, 1f)
        outlineShader.setUniformf(outlineShader.getUniformLocation("u_outlineColor"), OUTLINE_COLOR)
        with(graphics.sprite) {
            outlineShader.setUniformf(
                outlineShader.getUniformLocation("u_textureSize"),
                texture.width.toFloat(),
                texture.height.toFloat()
            )
            draw(batch)
        }
        batch.shader = null
    }

    private fun applyColorTintShader(batch: SpriteBatch, entity: Entity) {
        val player = entity.playerComponent
        val graphics = entity.graphicsComponent

        batch.shader = colorTintShader
        TINT_COLOR.a = clamp(1f - player.life / player.maxLife, 0f, 0.75f)
        colorTintShader.setUniformf(colorTintShader.getUniformLocation("u_tintColor"), TINT_COLOR)
        graphics.sprite.draw(batch)
        batch.shader = null
    }

    companion object {
        private val LOG = logger<RenderSystem>()
        private val OUTLINE_COLOR = Color(0f, 113 / 255f, 214 / 255f, 1f)
        private val TINT_COLOR = Color(1.0f, 0f, 0f, 1f)

        private const val SCROLL_SPEED_HORIZONTAL = 0.03f
        private const val SCROLL_SPEED_VERTICAL = -0.25f
    }
}
