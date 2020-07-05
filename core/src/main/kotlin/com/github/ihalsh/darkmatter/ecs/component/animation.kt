package com.github.ihalsh.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.github.ihalsh.darkmatter.ecs.component.AnimationType.*
import ktx.ashley.mapperFor
import ktx.collections.GdxArray

private const val DEFAULT_FRAME_DURATION = 1 / 20f

enum class AnimationType(val atlasKey: String, val playMode: PlayMode = LOOP, val speedRate: Float = 1f) {
    NONE(""),
    DARK_MATTER("dark_matter", speedRate = 3f),
    FIRE("fire"),
    SPEED_1("orb_blue"),
    SPEED_2("orb_yellow"),
    LIFE("life"),
    SHIELD("shield")

}

class Animation2D(
        val type: AnimationType,
        keyFrames: GdxArray<out TextureRegion>,
        playMode: PlayMode = LOOP,
        speedRate: Float = 1f) : Animation<TextureRegion>((DEFAULT_FRAME_DURATION) / speedRate, keyFrames, playMode)

class AnimationComponent : Component, Pool.Poolable {
    var type = NONE
    var stateTime = 0f
    lateinit var animation : Animation2D

    override fun reset() {
        type = NONE
        stateTime = 0f
    }

    companion object {
        val mapper = mapperFor<AnimationComponent>()
    }
}