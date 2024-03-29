package com.github.ihalsh.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.ashley.mapperFor

class GraphicComponent : Component, Poolable {
    val sprite = Sprite()

    override fun reset() {
        sprite.run {
            texture = null
            setColor(1f, 1f, 1f, 1f)
        }
    }

    fun setSpriteRegion(region: TextureRegion) = with(sprite) {
        setRegion(region)
        setSize(texture.width * UNIT_SCALE, texture.height * UNIT_SCALE)
        setOriginCenter()
    }

    companion object {
        val mapper = mapperFor<GraphicComponent>()

        private const val UNIT_SCALE = 1 / 16f
    }
}
