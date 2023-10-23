package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.github.ihalsh.darkmatter.ecs.component.AttachComponent
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import com.github.ihalsh.darkmatter.ecs.component.RemoveComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import com.github.ihalsh.darkmatter.utils.attachComponent
import com.github.ihalsh.darkmatter.utils.graphicsComponent
import com.github.ihalsh.darkmatter.utils.transformComponent
import ktx.ashley.addComponent
import ktx.ashley.allOf
import ktx.ashley.get

class AttachSystem : IteratingSystem(
    allOf(AttachComponent::class, TransformComponent::class, GraphicComponent::class).get()
), EntityListener {
    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }

    override fun entityAdded(entity: Entity) = Unit

    override fun entityRemoved(removedEntity: Entity) {
        entities.forEach { entity ->
            if (entity.attachComponent.entity == removedEntity) {
                entity.addComponent<RemoveComponent>(engine)
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val attach = entity.attachComponent
        val graphics = entity.graphicsComponent
        val transform = entity.transformComponent

        // Update position
        attach.entity[TransformComponent.mapper]?.let { attachTransform ->
            transform.interpolatedPosition.set(
                attachTransform.interpolatedPosition.x + attach.offset.x,
                attachTransform.interpolatedPosition.y + attach.offset.y,
                transform.position.z
            )
        }

        // Update graphic alpha value
        attach.entity[GraphicComponent.mapper]?.let { attachGraphic ->
            graphics.sprite.setAlpha(attachGraphic.sprite.color.a)
        }
    }
}
