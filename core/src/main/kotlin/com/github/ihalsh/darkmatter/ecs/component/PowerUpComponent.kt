package com.github.ihalsh.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable
import com.github.ihalsh.darkmatter.ecs.asset.SoundAsset
import com.github.ihalsh.darkmatter.ecs.asset.SoundAsset.*
import ktx.ashley.mapperFor

enum class PowerUpType(val animationType: AnimationType,
                       val lifeGain: Float = 0f,
                       val shieldGain: Float = 0f,
                       val speedGain: Float = 0f,
                       val soundAsset: SoundAsset
) {
    NONE(AnimationType.NONE, soundAsset = BLOCK),
    SPEED_1(AnimationType.SPEED_1, speedGain = 3f, soundAsset = BOOST_1),
    SPEED_2(AnimationType.SPEED_2, speedGain = 3.75f, soundAsset = BOOST_2),
    LIFE(AnimationType.LIFE, lifeGain = 25f, soundAsset = SoundAsset.LIFE),
    SHIELD(AnimationType.SHIELD, shieldGain = 25f, soundAsset = SoundAsset.SHIELD)
}

class PowerUpComponent : Component, Poolable {
    var type = PowerUpType.NONE

    override fun reset() {
        type = PowerUpType.NONE
    }

    companion object {
        val mapper = mapperFor<PowerUpComponent>()
    }
}