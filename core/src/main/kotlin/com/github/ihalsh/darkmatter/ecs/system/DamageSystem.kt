package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import com.github.ihalsh.darkmatter.ecs.component.RemoveComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import ktx.ashley.addComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.logger
import kotlin.math.max

const val DAMAGE_AREA_HEIGHT = 2f
private const val DAMAGE_PER_SECOND = 25f
private const val DEATH_EXPLOSION_DURATION = 0.9f

class DamageSystem : IteratingSystem(allOf(PlayerComponent::class, TransformComponent::class)
        .exclude(RemoveComponent::class.java).get()) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "$entity should have TransformComponent." }
        val player = entity[PlayerComponent.mapper]
        require(player != null) { "$entity should have PlayerComponent." }

        if (transform.position.y <= DAMAGE_AREA_HEIGHT) {
            val damage = DAMAGE_PER_SECOND * deltaTime
            if (player.shield > 0) player.shield.run {
                player.shield = max(0f, this - damage)
                if ((damage - this) <= 0) return
            }
            player.life -= damage
            if (player.life <= 0) entity.addComponent<RemoveComponent>(engine) { delay = DEATH_EXPLOSION_DURATION }
        }
    }
}