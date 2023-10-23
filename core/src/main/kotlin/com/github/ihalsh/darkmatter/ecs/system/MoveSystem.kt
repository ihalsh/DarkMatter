package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.github.ihalsh.darkmatter.di.DarkMatterContext.Companion.WORLD_HEIGHT
import com.github.ihalsh.darkmatter.di.DarkMatterContext.Companion.WORLD_WIDTH
import com.github.ihalsh.darkmatter.di.providers.EventDispatcher
import com.github.ihalsh.darkmatter.ecs.component.FacingComponent
import com.github.ihalsh.darkmatter.ecs.component.MoveComponent
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import com.github.ihalsh.darkmatter.ecs.component.RemoveComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import com.github.ihalsh.darkmatter.event.GameEvent
import com.github.ihalsh.darkmatter.utils.moveComponent
import com.github.ihalsh.darkmatter.utils.transformComponent
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.inject.Context
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MoveSystem(context: Context) : IteratingSystem(
    allOf(TransformComponent::class, MoveComponent::class).exclude(RemoveComponent::class).get()
) {
    private val eventDispatcher = context.inject<EventDispatcher>()
    private var accumulator = 0f

    // We're using a fixed time step
    override fun update(deltaTime: Float) {
        accumulator += deltaTime
        while (accumulator >= UPDATE_RATE) {
            accumulator -= UPDATE_RATE

            entities.forEach { entity ->
                entity[TransformComponent.mapper]?.let { transform ->
                    transform.prevPosition.set(transform.position)
                }
            }

            super.update(UPDATE_RATE)
        }

        // More info here: https://gafferongames.com/post/fix_your_timestep/
        val alpha = accumulator / UPDATE_RATE
        entities.forEach { entity ->
            entity[TransformComponent.mapper]?.let { transform ->
                transform.interpolatedPosition.set(
                    MathUtils.lerp(transform.prevPosition.x, transform.position.x, alpha),
                    MathUtils.lerp(transform.prevPosition.y, transform.position.y, alpha),
                    transform.position.z
                )
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity.transformComponent
        val move = entity.moveComponent

        val player = entity[PlayerComponent.mapper]
        val facing = entity[FacingComponent.mapper]

        if (player != null && facing != null) {
            // We should update the player differently
            movePlayer(transform, move, player, facing, deltaTime)
        } else {
            moveEntity(transform, move, deltaTime)
        }
    }

    private fun movePlayer(
        transform: TransformComponent,
        move: MoveComponent,
        player: PlayerComponent,
        facing: FacingComponent,
        deltaTime: Float
    ) {
        // update horizontal speed
        move.speed.x = when (facing.direction) {
            FacingComponent.Direction.LEFT -> min(0f, move.speed.x - HORIZONTAL_ACCELERATION * deltaTime)
            FacingComponent.Direction.RIGHT -> max(0f, move.speed.x + HORIZONTAL_ACCELERATION * deltaTime)
            else -> 0f
        }
        move.speed.x = MathUtils.clamp(move.speed.x, -MAX_HORIZONTAL_SPEED, MAX_HORIZONTAL_SPEED)

        // update vertical speed
        move.speed.y = MathUtils.clamp(
            move.speed.y - VERTICAL_ACCELERATION * deltaTime,
            -MAX_VERTICAL_NEGATIVE_PLAYER_SPEED,
            MAX_VERTICAL_POSITIVE_PLAYER_SPEED
        )

        transform.position.y.let { previousPositionY ->
            moveEntity(transform, move, deltaTime)
            val delta = abs(transform.position.y - previousPositionY)
            if (delta != 0f) {
                player.distance += delta
                eventDispatcher.dispatchEvent(GameEvent.PlayerMove::class) {
                    distance = player.distance
                    speed = move.speed.y
                }
            }
        }
    }

    private fun moveEntity(
        transform: TransformComponent,
        move: MoveComponent,
        deltaTime: Float
    ) {
        // update horizontal position
        transform.position.x = MathUtils.clamp(
            transform.position.x + move.speed.x * deltaTime,
            0f,
            WORLD_WIDTH - transform.size.x
        )

        // update vertical position
        transform.position.y = MathUtils.clamp(
            transform.position.y + move.speed.y * deltaTime,
            DARK_MATTER_HEIGHT,
            WORLD_HEIGHT + DARK_MATTER_HEIGHT - transform.size.y
        )
    }

    companion object {
        private const val DARK_MATTER_HEIGHT = 1f
        private const val HORIZONTAL_ACCELERATION = 16.5f
        private const val VERTICAL_ACCELERATION = 2.25f
        private const val MAX_VERTICAL_NEGATIVE_PLAYER_SPEED = 0.75f
        private const val MAX_VERTICAL_POSITIVE_PLAYER_SPEED = 5f
        private const val MAX_HORIZONTAL_SPEED = 5.5f

        private const val UPDATE_RATE = 1 / 25f
    }
}
