package com.github.ihalsh.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.*
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.collections.GdxArray

class AnimationComponent : Component, Pool.Poolable {

    lateinit var animation: Animation2D
    var type = AnimationType.NONE
    var stateTime = 0f

    override fun reset() {
        type = AnimationType.NONE
        stateTime = 0f
    }

    class Animation2D(
        val type: AnimationType,
        keyFrames: GdxArray<out TextureRegion>,
        playMode: PlayMode = PlayMode.LOOP,
        speedRate: Float = 1f
    ) : Animation<TextureRegion>(DEFAULT_FRAME_DURATION / speedRate, keyFrames, playMode)

    enum class AnimationType(
        val atlasKey: String,
        val playMode: PlayMode = PlayMode.LOOP,
        val speedRate: Float = 1f
    ) {
        NONE(""),
        DARK_MATTER("dark_matter", speedRate = 3f),
        FIRE("fire"),
        SPEED_1("orb_blue", speedRate = 0.5f),
        SPEED_2("orb_yellow", speedRate = 0.5f),
        LIFE("life"),
        SHIELD("shield", speedRate = 0.75f),
        EXPLOSION("explosion", PlayMode.NORMAL, speedRate = 0.5f)
    }

    companion object {
        val mapper = mapperFor<AnimationComponent>()

        const val DEFAULT_FRAME_DURATION = 1 / 20f
    }
}
