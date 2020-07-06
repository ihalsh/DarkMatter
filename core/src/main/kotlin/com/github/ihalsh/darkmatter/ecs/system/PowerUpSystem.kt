package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils.random
import com.badlogic.gdx.math.Rectangle
import com.github.ihalsh.darkmatter.V_WIDTH
import com.github.ihalsh.darkmatter.ecs.component.*
import com.github.ihalsh.darkmatter.ecs.component.PowerUpType.*
import com.github.ihalsh.darkmatter.event.GameEventCollectPowerUp
import com.github.ihalsh.darkmatter.event.GameEventManager
import com.github.ihalsh.darkmatter.event.GameEventType
import com.github.ihalsh.darkmatter.event.GameEventType.*
import ktx.ashley.*
import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf
import ktx.log.debug
import ktx.log.error
import ktx.log.logger
import kotlin.*
import kotlin.math.min

private val LOG = logger<PowerUpSystem>()
private const val MAX_SPAWN_INTERVAL = 1.5f
private const val MIN_SPAWN_INTERVAL = 0.9f
private const val POWER_UP_SPEED = -8.75f
private const val BOOST_1_SPEED_GAIN = 3f
private const val BOOST_2_SPEED_GAIN = 3.75f
private const val LIFE_GAIN = 25f
private const val SHIELD_GAIN = 25f

private class SpawnPattern(
        type1: PowerUpType = NONE,
        type2: PowerUpType = NONE,
        type3: PowerUpType = NONE,
        type4: PowerUpType = NONE,
        type5: PowerUpType = NONE,
        val types: GdxArray<PowerUpType> = gdxArrayOf(type1, type2, type3, type4, type5)
)

class PowerUpSystem(private val gameEventManager: GameEventManager) : IteratingSystem(allOf(PowerUpComponent::class,
        TransformComponent::class).exclude(RemoveComponent::class).get()) {
    private val playerBoundingRectangle = Rectangle()
    private val powerUpBoundingRectangle = Rectangle()
    private val playerEntities by lazy {
        engine.getEntitiesFor(
                allOf(PlayerComponent::class).exclude(RemoveComponent::class).get())
    }
    private var spawnTime = 0f
    private val spawnPatterns = gdxArrayOf(
            SpawnPattern(type1 = SPEED_1, type2 = SPEED_2, type5 = SHIELD),
            SpawnPattern(type1 = SPEED_2, type2 = LIFE, type5 = SPEED_1),
            SpawnPattern(type2 = SPEED_1, type4 = SPEED_1, type5 = SPEED_1),
            SpawnPattern(type2 = SPEED_1, type4 = SPEED_1),
            SpawnPattern(type1 = SHIELD, type2 = SHIELD, type4 = LIFE, type5 = SPEED_2)
    )
    private val currentSpawnPattern = GdxArray<PowerUpType>(spawnPatterns.size)

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        spawnTime -= deltaTime
        if (spawnTime <= 0f) {
            spawnTime = random(MIN_SPAWN_INTERVAL, MAX_SPAWN_INTERVAL)

            if (currentSpawnPattern.isEmpty)
                currentSpawnPattern.addAll(spawnPatterns[random(0, spawnPatterns.size - 1)].types)
                        .also { LOG.debug { "Next pattern: $currentSpawnPattern" } }

            currentSpawnPattern.removeIndex(0).run {
                if (this == NONE) return
                spawnPowerUp(this, x = 1f * random(0, V_WIDTH - 1), y = 16f)
            }
        }
    }

    private fun spawnPowerUp(powerUpType: PowerUpType, x: Float, y: Float) {
        engine.entity {
            with<TransformComponent> { setInitialPosition(x, y, 0f) }
            with<PowerUpComponent> { type = powerUpType }
            with<AnimationComponent> { type = powerUpType.animationType }
            with<GraphicComponent>()
            with<MoveComponent> { speed.y = POWER_UP_SPEED }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "$entity should have TransformComponent." }

        if (transform.position.y <= 1) {
            entity.addComponent<RemoveComponent>(engine)
            return
        }

        powerUpBoundingRectangle.set(
                transform.position.x,
                transform.position.y,
                transform.size.x,
                transform.size.y)

        playerEntities.forEach { player ->
            player[TransformComponent.mapper]?.let { playerTransform ->
                playerBoundingRectangle.set(
                        playerTransform.position.x,
                        playerTransform.position.y,
                        playerTransform.size.x,
                        playerTransform.size.y)
                if (playerBoundingRectangle.overlaps(powerUpBoundingRectangle)) collectPowerUp(player, entity)
            }
        }
    }

    private fun collectPowerUp(player: Entity, powerUp: Entity) {
        val powerUpCmp = powerUp[PowerUpComponent.mapper]
        require(powerUpCmp != null) { "$powerUp should have PowerUpComponent." }

        LOG.debug { "Picking up power of type ${powerUpCmp.type}" }

        when (powerUpCmp.type) {
            SPEED_1 -> player[MoveComponent.mapper]?.let { it.speed.y += BOOST_1_SPEED_GAIN }
            SPEED_2 -> player[MoveComponent.mapper]?.let { it.speed.y += BOOST_2_SPEED_GAIN }
            LIFE -> player[PlayerComponent.mapper]?.let { it.life = min(it.maxLife, it.life + LIFE_GAIN) }
            SHIELD -> player[PlayerComponent.mapper]?.let { it.shield = min(it.maxShield, it.shield + SHIELD_GAIN) }
            else -> LOG.error { "Unsupported power of type ${powerUpCmp.type}" }
        }

        gameEventManager.dispatchEvent(COLLECT_POWER_UP, GameEventCollectPowerUp.apply {
                    this.player = player
                    this.type = powerUpCmp.type
                })

        powerUp.addComponent<RemoveComponent>(engine)
    }
}