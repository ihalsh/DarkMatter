package com.github.ihalsh.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.utils.Pool.*
import ktx.ashley.mapperFor

class GraphicComponent : Component, Poolable {
    val sprite = Sprite()

    override fun reset() {
        sprite.run {
            texture = null
            setColor(1f, 1f, 1f, 1f)
        }
    }
    companion object {
        val mapper = mapperFor<GraphicComponent>()
    }
}