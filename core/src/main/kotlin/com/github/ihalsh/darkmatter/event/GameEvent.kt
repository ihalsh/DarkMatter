package com.github.ihalsh.darkmatter.event

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import com.github.ihalsh.darkmatter.asset.SoundAsset
import com.github.ihalsh.darkmatter.ecs.component.PowerUpComponent.PowerUpType

sealed interface GameEvent : Pool.Poolable {
    val soundAsset: SoundAsset?

    override fun reset() = Unit

    class RestartGame : GameEvent {
        override val soundAsset = null
    }

    class GameOver : GameEvent {
        override val soundAsset: SoundAsset = SoundAsset.EXPLOSION
        var distance = 0f

        override fun toString(): String = "${GameOver::class.java.simpleName}(distance=${distance})"
    }

    class PowerUpCollected : GameEvent {
        override val soundAsset
            get() = when (type) {
                PowerUpType.SPEED_1 -> SoundAsset.BOOST_1
                PowerUpType.SPEED_2 -> SoundAsset.BOOST_2
                PowerUpType.LIFE -> SoundAsset.LIFE
                PowerUpType.SHIELD -> SoundAsset.SHIELD
                else -> SoundAsset.BLOCK
            }
        var type: PowerUpType = PowerUpType.NONE
        lateinit var player: Entity


        override fun toString(): String = "${PowerUpCollected::class.java.simpleName}(type=${type})"
    }

    class ShipDamaged : GameEvent {
        override val soundAsset = SoundAsset.DAMAGE
        var life = 0f
        var maxLife = 0f

        override fun toString(): String = "${ShipDamaged::class.java.simpleName}(life=$life, maxLife=$maxLife)"
    }

    class PlayerMove : GameEvent {
        override val soundAsset = null
        var distance = 0f
        var speed = 0f

        override fun toString() = "PlayerMove(distance=$distance,speed=$speed)"
    }

    class PlayerSpawn : GameEvent {
        override val soundAsset = SoundAsset.SPAWN
    }
}
