package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils.clamp
import com.badlogic.gdx.math.MathUtils.lerp
import com.github.ihalsh.darkmatter.V_HEIGHT
import com.github.ihalsh.darkmatter.V_WIDTH
import com.github.ihalsh.darkmatter.ecs.component.*
import com.github.ihalsh.darkmatter.ecs.component.FacingDirection.LEFT
import com.github.ihalsh.darkmatter.ecs.component.FacingDirection.RIGHT
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import kotlin.math.max
import kotlin.math.min

private const val UPDATE_RATE = 1 / 25f
private const val HOR_ACCELERATION = 16.5f
private const val VER_ACCELERATION = 2.25f
private const val MAX_VER_NEG_PLAYER_SPEED = 0.75f
private const val MAX_VER_POS_PLAYER_SPEED = 5f
private const val MAX_HOR_SPEED = 5.5f

class MoveSystem : IteratingSystem(allOf(TransformComponent::class, MoveComponent::class)
        .exclude(RemoveComponent::class).get()) {
    private var acc = 0f

    override fun update(deltaTime: Float) {
        acc += deltaTime
        while (acc >= UPDATE_RATE) {
            acc -= UPDATE_RATE

            entities.forEach { entity ->
                entity[TransformComponent.mapper]?.let { transform ->
                    transform.prevPosition.set(transform.position)
                }
            }
            super.update(UPDATE_RATE)
        }
        val alpha = acc / UPDATE_RATE
        entities.forEach { entity ->
            entity[TransformComponent.mapper]?.let { transform ->
                transform.interpolatedPosition.set(
                        lerp(transform.prevPosition.x, transform.position.x, alpha),
                        lerp(transform.prevPosition.y, transform.position.y, alpha),
                        transform.position.z)
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "$entity should have TransformComponent." }
        val move = entity[MoveComponent.mapper]
        require(move != null) { "$entity should have MoveComponent." }

        val player = entity[PlayerComponent.mapper]
        if (player != null)
            entity[FacingComponent.mapper]?.let { facing ->
                movePlayer(transform, move, player, facing, deltaTime)
            }
        else moveEntity(transform, move, deltaTime)
    }

    private fun movePlayer(transform: TransformComponent,
                           move: MoveComponent,
                           player: PlayerComponent,
                           facing: FacingComponent,
                           deltaTime: Float) {

        move.speed.x = when (facing.direction) {
            LEFT -> min(0f, move.speed.x - HOR_ACCELERATION * deltaTime)
            RIGHT -> max(0f, move.speed.x + HOR_ACCELERATION * deltaTime)
            else -> 0f
        }.run { clamp(this, -MAX_HOR_SPEED, MAX_HOR_SPEED) }

        move.speed.y = (move.speed.y - VER_ACCELERATION * deltaTime).run {
            clamp(this, -MAX_VER_NEG_PLAYER_SPEED, MAX_VER_POS_PLAYER_SPEED)
        }

        moveEntity(transform, move, deltaTime)
    }

    private fun moveEntity(transform: TransformComponent, move: MoveComponent, deltaTime: Float) {
        transform.position.x = (transform.position.x + move.speed.x * deltaTime).run {
            clamp(this, 0f, V_WIDTH - transform.size.x)
        }
        transform.position.y = (transform.position.y + move.speed.y * deltaTime).run {
            clamp(this, 1f, V_HEIGHT + 1f - transform.size.y)
        }
    }
}