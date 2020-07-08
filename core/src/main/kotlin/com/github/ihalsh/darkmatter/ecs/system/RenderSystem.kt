package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import com.github.ihalsh.darkmatter.ecs.component.PowerUpType.SPEED_1
import com.github.ihalsh.darkmatter.ecs.component.PowerUpType.SPEED_2
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import com.github.ihalsh.darkmatter.event.*
import com.github.ihalsh.darkmatter.event.GameEvent.*
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<RenderSystem>()

class RenderSystem(private val batch: Batch,
                   private val gameViewport: Viewport,
                   private val uiViewport: Viewport,
                   backgroundTexture: Texture,
                   private val gameEventManager: GameEventManager) : SortedIteratingSystem(
        allOf(TransformComponent::class, GraphicComponent::class).get(),
        compareBy { entity -> entity[TransformComponent.mapper] }), GameEventListener {
    private val background = Sprite(backgroundTexture.apply { setWrap(Repeat, Repeat) })
    private val backgroundScrollSpeed = Vector2(0.03f, -0.25f)

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(CollectPowerUp::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(CollectPowerUp::class, this)
    }

    override fun update(deltaTime: Float) {
        forceSort()
        uiViewport.apply()
        batch.use(uiViewport.camera.combined) {
            background.run {
                backgroundScrollSpeed.y = minOf(-0.25f, backgroundScrollSpeed.y + deltaTime * (0.1f))
                scroll(backgroundScrollSpeed.x * deltaTime, backgroundScrollSpeed.y * deltaTime)
                draw(it)
            }
        }
        gameViewport.apply()
        batch.use(gameViewport.camera.combined) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "$entity should have TransformComponent to be rendered." }
        val graphics = entity[GraphicComponent.mapper]
        require(graphics != null) { "$entity should have GraphicComponent to be rendered." }

        if (graphics.sprite.texture == null) {
            LOG.debug { "Entity $entity has no texture for rendering." }
            return
        }

        graphics.sprite.run {
            rotation = transform.rotationDeg
            setBounds(
                    transform.interpolatedPosition.x,
                    transform.interpolatedPosition.y,
                    transform.size.x,
                    transform.size.y)
            draw(batch)
        }
    }

    override fun onEvent(event: GameEvent) {
        (event as CollectPowerUp).run {
            if (this.type == SPEED_1) backgroundScrollSpeed.y -= 0.25f
            else if (this.type == SPEED_2) backgroundScrollSpeed.y -= 0.5f
        }
    }
}