package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils.random
import com.badlogic.gdx.math.Rectangle
import com.github.ihalsh.darkmatter.di.DarkMatterContext.Companion.WORLD_HEIGHT
import com.github.ihalsh.darkmatter.di.DarkMatterContext.Companion.WORLD_WIDTH
import com.github.ihalsh.darkmatter.di.providers.EventDispatcher
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import com.github.ihalsh.darkmatter.ecs.component.PowerUpComponent
import com.github.ihalsh.darkmatter.ecs.component.PowerUpComponent.PowerUpType
import com.github.ihalsh.darkmatter.ecs.component.PowerUpComponent.PowerUpType.LIFE
import com.github.ihalsh.darkmatter.ecs.component.PowerUpComponent.PowerUpType.NONE
import com.github.ihalsh.darkmatter.ecs.component.PowerUpComponent.PowerUpType.SHIELD
import com.github.ihalsh.darkmatter.ecs.component.PowerUpComponent.PowerUpType.SPEED_1
import com.github.ihalsh.darkmatter.ecs.component.PowerUpComponent.PowerUpType.SPEED_2
import com.github.ihalsh.darkmatter.ecs.component.RemoveComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import com.github.ihalsh.darkmatter.event.GameEvent.PowerUpCollected
import com.github.ihalsh.darkmatter.utils.addPowerUp
import com.github.ihalsh.darkmatter.utils.moveComponent
import com.github.ihalsh.darkmatter.utils.playerComponent
import com.github.ihalsh.darkmatter.utils.powerUpComponent
import com.github.ihalsh.darkmatter.utils.transformComponent
import ktx.ashley.addComponent
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf
import ktx.inject.Context
import ktx.log.logger

class PowerUpSystem(context: Context) : IteratingSystem(
    allOf(PowerUpComponent::class, TransformComponent::class).exclude(RemoveComponent::class).get()
) {
    private var spawnTime = 0f

    private val eventDispatcher = context.inject<EventDispatcher>()
    private val playerBoundingRectangle = Rectangle()
    private val powerUpBoundingRectangle = Rectangle()
    private val playerEntities by lazy {
        engine.getEntitiesFor(
            allOf(PlayerComponent::class).exclude(RemoveComponent::class).get()
        )
    }
    private val randomPowerUps = {
        gdxArrayOf(
            SpawnPattern(type1 = SPEED_1, type2 = SPEED_2, type5 = LIFE),
            SpawnPattern(type2 = LIFE, type3 = SHIELD, type4 = SPEED_2)
        ).let { spawnPatterns ->
            spawnPatterns[random(0, spawnPatterns.size - 1)].types
        }
    }

    private val currentSpawnPattern = GdxArray<PowerUpType>()

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        spawnTime -= deltaTime
        if (spawnTime <= 0f) {
            spawnTime = random(MIN_SPAWN_INTERVAL, MAX_SPAWN_INTERVAL)

            with(currentSpawnPattern) {
                if (isEmpty) addAll(randomPowerUps()).also { LOG.debug { "Next pattern: $this" } }

                removeIndex(0)
                    .takeIf { it != NONE }
                    ?.let { powerUpType -> spawnPowerUp(powerUpType) }
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity.transformComponent

        if (transform.position.y <= 1f) {
            entity.addComponent<RemoveComponent>(engine)
            return
        }

        powerUpBoundingRectangle.set(
            transform.position.x,
            transform.position.y,
            transform.size.x,
            transform.size.y
        )

        playerEntities.forEach { player ->
            player[TransformComponent.mapper]?.let { playerTransform ->
                playerBoundingRectangle.set(
                    playerTransform.position.x,
                    playerTransform.position.y,
                    playerTransform.size.x,
                    playerTransform.size.y
                )

                if (playerBoundingRectangle.overlaps(powerUpBoundingRectangle)) {
                    collectPowerUp(player, entity)
                }
            }
        }
    }

    fun reset() {
        spawnTime = 0f
        entities.forEach {
            it.addComponent<RemoveComponent>(engine)
        }
    }

    private fun spawnPowerUp(powerUpType: PowerUpType) {
        engine.addPowerUp(powerUpType, 1f * random(0, WORLD_WIDTH.toInt() - 1), WORLD_HEIGHT)
    }

    private fun collectPowerUp(player: Entity, powerUpEntity: Entity) {
        val powerUpComponent = powerUpEntity.powerUpComponent

        with(powerUpComponent.type) {
            player.moveComponent.speed.y += speedGain
            player.playerComponent.run {
                life = minOf(maxLife, life + lifeGain)
                shield = minOf(maxShield, shield + shieldGain)
            }
            eventDispatcher.dispatchEvent(PowerUpCollected::class) {
                type = this@with
                this.player = player
            }
        }

        powerUpEntity.addComponent<RemoveComponent>(engine)
    }

    companion object {
        private val LOG = logger<PowerUpComponent>()

        private const val MAX_SPAWN_INTERVAL = 1.5f
        private const val MIN_SPAWN_INTERVAL = 0.9f

        private class SpawnPattern(
            type1: PowerUpType = NONE,
            type2: PowerUpType = NONE,
            type3: PowerUpType = NONE,
            type4: PowerUpType = NONE,
            type5: PowerUpType = NONE,
            val types: GdxArray<PowerUpType> = gdxArrayOf(type1, type2, type3, type4, type5)
        )
    }
}
