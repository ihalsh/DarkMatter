package com.github.ihalsh.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class AttachComponent : Component, Pool.Poolable {
    lateinit var masterEntity: Entity
    val offset = Vector2()

    override fun reset() {
        offset.set(Vector2.Zero)
    }

    companion object {
        val mapper = mapperFor<AttachComponent>()
    }
}