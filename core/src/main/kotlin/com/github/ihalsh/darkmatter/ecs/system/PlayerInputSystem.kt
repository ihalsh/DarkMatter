package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.ihalsh.darkmatter.ecs.component.FacingComponent
import com.github.ihalsh.darkmatter.ecs.component.FacingDirection.*
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.get

private const val TOUCH_TOLERANCE = 0.2f

class PlayerInputSystem(private val gameViewport: Viewport) : IteratingSystem(allOf(PlayerComponent::class,
        TransformComponent::class,
        FacingComponent::class).get()) {
    private val tmpVector = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "$entity should have TransformComponent." }
        val facing = entity[FacingComponent.mapper]
        require(facing != null) { "$entity should have FacingComponent." }

        tmpVector.x = Gdx.input.x.toFloat()
        gameViewport.unproject(tmpVector)
        (tmpVector.x - transform.position.x - transform.size.x * 0.5).let { diffX ->
            facing.direction = when {
                diffX < -TOUCH_TOLERANCE -> LEFT
                diffX > TOUCH_TOLERANCE -> RIGHT
                else -> DEFAULT
            }
        }
    }
}