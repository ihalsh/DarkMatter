package com.github.ihalsh.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.ashley.mapperFor

class TransformComponent : Component, Poolable, Comparable<TransformComponent> {
    val position = Vector3()
    val size = Vector2(1f, 1f)
    var rotationDeg = 0f

    override fun reset() {
        position.set(Vector3())
        size.set(1f, 1f)
        rotationDeg = 0f
    }
    override fun compareTo(other: TransformComponent): Int {
        val zDifference = (position.z - other.position.z).toInt()
        return if (zDifference != 0) zDifference else (position.y - other.position.y).toInt()
    }
    companion object {
        val mapper = mapperFor<TransformComponent>()
    }
}