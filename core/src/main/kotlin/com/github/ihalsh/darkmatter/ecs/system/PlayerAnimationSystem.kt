package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.github.ihalsh.darkmatter.ecs.component.FacingComponent
import com.github.ihalsh.darkmatter.ecs.component.FacingDirection.*
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import ktx.ashley.allOf
import ktx.ashley.get

class PlayerAnimationSystem(
        private val defaultRegion: TextureRegion,
        private val leftRegion: TextureRegion,
        private val rightRegion: TextureRegion
) : IteratingSystem(allOf(PlayerComponent::class, FacingComponent::class, GraphicComponent::class).get()),
        EntityListener {
    private var lastDirection = DEFAULT

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(family, this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }

    override fun entityAdded(entity: Entity) {
        entity[GraphicComponent.mapper]?.setSpriteRegion(defaultRegion)
    }

    override fun entityRemoved(entity: Entity) = Unit

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val graphics = entity[GraphicComponent.mapper]
        require(graphics != null) { "$entity should have GraphicComponent to be rendered." }
        val facing = entity[FacingComponent.mapper]
        require(facing != null) { "$entity should have FacingComponent." }

        if (facing.direction == lastDirection && graphics.sprite.texture != null) return

        lastDirection = facing.direction

        graphics.setSpriteRegion(when (facing.direction) {
            LEFT -> leftRegion
            RIGHT -> rightRegion
            else -> defaultRegion
        })
    }
}
