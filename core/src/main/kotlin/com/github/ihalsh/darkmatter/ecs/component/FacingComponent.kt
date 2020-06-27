package com.github.ihalsh.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable
import com.github.ihalsh.darkmatter.ecs.component.FacingDirection.DEFAULT
import ktx.ashley.mapperFor

class FacingComponent : Component, Poolable {
    var direction = DEFAULT

    override fun reset() {
        direction = DEFAULT
    }

    companion object {
        val mapper = mapperFor<FacingComponent>()
    }
}

enum class FacingDirection {
    LEFT, DEFAULT, RIGHT
}