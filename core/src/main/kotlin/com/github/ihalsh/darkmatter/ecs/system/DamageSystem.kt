package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.github.ihalsh.darkmatter.di.providers.EventDispatcher
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import com.github.ihalsh.darkmatter.ecs.component.RemoveComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import com.github.ihalsh.darkmatter.event.GameEvent.GameOver
import com.github.ihalsh.darkmatter.event.GameEvent.ShipDamaged
import com.github.ihalsh.darkmatter.utils.addExplosion
import com.github.ihalsh.darkmatter.utils.playerComponent
import com.github.ihalsh.darkmatter.utils.transformComponent
import ktx.ashley.addComponent
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.inject.Context
import kotlin.math.max

class DamageSystem(context: Context) : IteratingSystem(
    allOf(PlayerComponent::class, TransformComponent::class).exclude(RemoveComponent::class).get()
) {
    private val eventDispatcher = context.inject<EventDispatcher>()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity.transformComponent
        val player = entity.playerComponent
        val isInDangerZone = transform.position.y <= DAMAGE_AREA_HEIGHT
        val damage by lazy { takeDamage(DAMAGE_PER_SECOND * deltaTime, player) }

        if (!isInDangerZone || damage == 0f) return

        reduceLife(damage, player)

        if (player.isDead) {
            removePlayer(entity)
            engine.addExplosion(transform.position.x, transform.position.y)
            eventDispatcher.dispatchEvent(GameOver::class) { distance = player.distance }
        } else {
            eventDispatcher.dispatchEvent(ShipDamaged::class) {
                life = player.life
                maxLife = player.maxLife
            }
        }
    }

    private fun takeDamage(initialDamage: Float, player: PlayerComponent): Float =
        if (player.hasShield) {
            player.shield.let { blockAmount ->
                player.shield = max(0f, blockAmount - initialDamage)
                max(0f, initialDamage - blockAmount)
            }
        } else {
            initialDamage
        }

    private fun reduceLife(damage: Float, player: PlayerComponent) {
        player.life -= damage
    }

    private fun removePlayer(entity: Entity) {
        entity.addComponent<RemoveComponent>(engine) {
            delay = DEATH_EXPLOSION_DURATION
        }
        // Hide ship
        entity[GraphicComponent.mapper]?.sprite?.setAlpha(0f)
    }

    companion object {
        const val DAMAGE_AREA_HEIGHT = 1.5f
        const val DAMAGE_PER_SECOND = 25f
        const val DEATH_EXPLOSION_DURATION = 0.9f
        const val DEATH_EXPLOSION_SIZE = 1.5f
    }
}
