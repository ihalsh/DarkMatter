package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.github.ihalsh.darkmatter.ecs.component.AttachComponent
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import com.github.ihalsh.darkmatter.ecs.component.RemoveComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import ktx.ashley.addComponent
import ktx.ashley.allOf
import ktx.ashley.get

class AttachSystem : IteratingSystem(allOf(AttachComponent::class,
        GraphicComponent::class,
        TransformComponent::class).get()), EntityListener {

    override fun addedToEngine(engine: Engine) {
        with(engine) {
            super.addedToEngine(this)
            addEntityListener(this@AttachSystem)
        }
    }

    override fun removedFromEngine(engine: Engine) {
        with(engine) {
            super.removedFromEngine(this)
            removeEntityListener(this@AttachSystem)
        }
    }

    override fun entityAdded(entity: Entity) = Unit

    override fun entityRemoved(removedEntity: Entity) {
        entities.forEach { entity ->
            entity[AttachComponent.mapper]?.let { attach ->
                if (attach.masterEntity == removedEntity) entity.addComponent<RemoveComponent>(engine)
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val attach = entity[AttachComponent.mapper]
        require(attach != null) { "$entity should have AttachComponent." }
        val graphic = entity[GraphicComponent.mapper]
        require(graphic != null) { "$entity should have GraphicComponent." }
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "$entity should have TransformComponent." }

        attach.masterEntity[TransformComponent.mapper]?.let { masterTransform ->
            transform.interpolatedPosition.set(
                    masterTransform.interpolatedPosition.x + attach.offset.x,
                    masterTransform.interpolatedPosition.y + attach.offset.y,
                    transform.position.z
            )
        }
        attach.masterEntity[GraphicComponent.mapper]?.let { attachGraphic ->
            graphic.sprite.setAlpha(attachGraphic.sprite.color.a)
        }
    }
}