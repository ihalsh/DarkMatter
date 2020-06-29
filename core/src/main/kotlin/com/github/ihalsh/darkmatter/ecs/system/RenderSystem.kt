package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<RenderSystem>()

class RenderSystem (private val batch: Batch, private val gameViewport : Viewport) : SortedIteratingSystem(
        allOf(TransformComponent::class, GraphicComponent::class).get(),
        compareBy {entity -> entity[TransformComponent.mapper]}
) {
    override fun update(deltaTime: Float) {
        forceSort()
        gameViewport.apply()
        batch.use(gameViewport.camera.combined) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) {"$entity should have TransformComponent to be rendered."}
        val graphics = entity[GraphicComponent.mapper]
        require(graphics != null) {"$entity should have GraphicComponent to be rendered."}

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
}