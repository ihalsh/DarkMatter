package com.github.ihalsh.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.github.ihalsh.darkmatter.ecs.component.AnimationComponent.AnimationType
import ktx.ashley.mapperFor

class PowerUpComponent : Component, Pool.Poolable {
    var type = PowerUpType.NONE

    override fun reset() {
        type = PowerUpType.NONE
    }

    enum class PowerUpType(
        val animationType: AnimationType,
        val lifeGain: Float = 0f,
        val shieldGain: Float = 0f,
        val speedGain: Float = 0f
    ) {
        NONE(AnimationType.NONE),
        SPEED_1(AnimationType.SPEED_1, speedGain = BOOST_1_SPEED_GAIN),
        SPEED_2(AnimationType.SPEED_2, speedGain = BOOST_2_SPEED_GAIN),
        LIFE(AnimationType.LIFE, lifeGain = LIFE_GAIN),
        SHIELD(AnimationType.SHIELD, shieldGain = SHIELD_GAIN)
    }

    companion object {
        val mapper = mapperFor<PowerUpComponent>()

        private const val BOOST_1_SPEED_GAIN = 3f
        private const val BOOST_2_SPEED_GAIN = 3.75f
        private const val LIFE_GAIN = 25f
        private const val SHIELD_GAIN = 25f
    }
}
