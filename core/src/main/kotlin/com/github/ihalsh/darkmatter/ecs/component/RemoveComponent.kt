package com.github.ihalsh.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.ashley.mapperFor

class RemoveComponent : Component, Poolable {
    var delay = 0f

    override fun reset() {
        delay = 0f
    }

    companion object {
        val mapper = mapperFor<RemoveComponent>()
    }
}