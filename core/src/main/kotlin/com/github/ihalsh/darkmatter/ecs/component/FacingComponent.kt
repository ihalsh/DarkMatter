package com.github.ihalsh.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.ashley.mapperFor

class FacingComponent : Component, Poolable {
    var direction = Direction.DEFAULT
    var previousDirection = Direction.DEFAULT

    override fun reset() {
        direction = Direction.DEFAULT
    }

    enum class Direction {
        LEFT, DEFAULT, RIGHT
    }

    companion object {
        val mapper = mapperFor<FacingComponent>()
    }
}
