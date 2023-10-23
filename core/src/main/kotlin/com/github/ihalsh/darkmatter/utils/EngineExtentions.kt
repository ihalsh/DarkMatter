package com.github.ihalsh.darkmatter.utils

import com.badlogic.ashley.core.Engine
import com.github.ihalsh.darkmatter.di.DarkMatterContext
import com.github.ihalsh.darkmatter.ecs.component.AnimationComponent
import com.github.ihalsh.darkmatter.ecs.component.AnimationComponent.AnimationType.DARK_MATTER
import com.github.ihalsh.darkmatter.ecs.component.AnimationComponent.AnimationType.EXPLOSION
import com.github.ihalsh.darkmatter.ecs.component.AnimationComponent.AnimationType.FIRE
import com.github.ihalsh.darkmatter.ecs.component.AttachComponent
import com.github.ihalsh.darkmatter.ecs.component.FacingComponent
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import com.github.ihalsh.darkmatter.ecs.component.MoveComponent
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import com.github.ihalsh.darkmatter.ecs.component.PowerUpComponent
import com.github.ihalsh.darkmatter.ecs.component.RemoveComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import com.github.ihalsh.darkmatter.ecs.system.DamageSystem
import ktx.ashley.entity
import ktx.ashley.with

private const val UNIT_SCALE = 1 / 16f
private const val POWER_UP_SPEED = -8.75f
private const val SHIP_FIRE_OFFSET_X = 1f // in pixels
private const val SHIP_FIRE_OFFSET_Y = -6f // in pixels
const val PLAYER_START_SPEED = 3f

fun Engine.addPlayerShip() = entity {
    with<TransformComponent> {
        setInitialPosition(4.5f, 7.5f, 1f)
    }
    with<MoveComponent> {
        speed.y = PLAYER_START_SPEED
    }
    with<GraphicComponent>()
    with<PlayerComponent>()
    with<FacingComponent>()
}.also { playerShip ->
    entity {
        with<TransformComponent>()
        with<AttachComponent> {
            entity = playerShip
            offset.set(
                SHIP_FIRE_OFFSET_X * UNIT_SCALE,
                SHIP_FIRE_OFFSET_Y * UNIT_SCALE
            )
        }
        with<GraphicComponent>()
        with<AnimationComponent> {
            type = FIRE
        }
    }
}

fun Engine.addDarkMatter() = entity {
    with<TransformComponent> {
        size.set(DarkMatterContext.WORLD_WIDTH, DamageSystem.DAMAGE_AREA_HEIGHT)
        setInitialPosition(0f, 0f, 0f)
    }
    with<AnimationComponent> {
        type = DARK_MATTER
    }
    with<GraphicComponent>()
}

fun Engine.addPowerUp(powerUpType: PowerUpComponent.PowerUpType, x: Float, y: Float) = entity {
    with<TransformComponent> {
        setInitialPosition(x, y, 0f)
    }
    with<PowerUpComponent> {
        type = powerUpType
    }
    with<AnimationComponent> {
        type = powerUpType.animationType
    }
    with<GraphicComponent>()
    with<MoveComponent> {
        speed.y = POWER_UP_SPEED
    }
}

fun Engine.addExplosion(x: Float, y: Float) = entity {
    with<TransformComponent> {
        size.set(DamageSystem.DEATH_EXPLOSION_SIZE, DamageSystem.DEATH_EXPLOSION_SIZE)
        setInitialPosition(x, y, 1f)
    }
    with<AnimationComponent> {
        type = EXPLOSION
    }
    with<GraphicComponent>()
    with<RemoveComponent> {
        delay = DamageSystem.DEATH_EXPLOSION_DURATION
    }
}
