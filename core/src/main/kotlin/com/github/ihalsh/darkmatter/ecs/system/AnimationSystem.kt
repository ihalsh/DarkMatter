package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.utils.GdxRuntimeException
import com.github.ihalsh.darkmatter.asset.TextureAtlasAsset
import com.github.ihalsh.darkmatter.ecs.component.AnimationComponent
import com.github.ihalsh.darkmatter.ecs.component.AnimationComponent.Animation2D
import com.github.ihalsh.darkmatter.ecs.component.AnimationComponent.AnimationType
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import com.github.ihalsh.darkmatter.utils.animationComponent
import com.github.ihalsh.darkmatter.utils.graphicsComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.assets.async.AssetStorage
import ktx.log.logger
import java.util.EnumMap

class AnimationSystem(asset: AssetStorage) : IteratingSystem(
    allOf(AnimationComponent::class, GraphicComponent::class).get()
), EntityListener {
    private val graphicAtlas = asset[TextureAtlasAsset.GAME_GRAPHICS.descriptor]
    private val animationCache = EnumMap<AnimationType, Animation2D>(AnimationType::class.java)

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(family, this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val animations = entity.animationComponent
        val graphics = entity.graphicsComponent

        if (animations.type == AnimationType.NONE) {
            LOG.error { "No type specified for animation component $animations for |entity| $entity" }
            return
        }

        if (animations.type == animations.animation.type) {
            animations.stateTime += deltaTime
        } else {
            animations.stateTime = 0f
            animations.animation = getAnimation(animations.type)
        }

        val frame = animations.animation.getKeyFrame(animations.stateTime)
        graphics.setSpriteRegion(frame)
    }

    override fun entityAdded(entity: Entity) {
        entity[AnimationComponent.mapper]?.let { animationComponent ->
            animationComponent.animation = getAnimation(animationComponent.type)
            val frame = animationComponent.animation.getKeyFrame(animationComponent.stateTime)
            entity[GraphicComponent.mapper]?.setSpriteRegion(frame)
        }
    }

    private fun getAnimation(type: AnimationType) =
        animationCache[type]
            .takeIf { it != null }
            ?: run {
                val regions = getAtlasRegions(type)

                Animation2D(type, regions, type.playMode, type.speedRate)
                    .also { animationCache[type] = it }
            }

    private fun getAtlasRegions(type: AnimationType) =
        findAtlasRegions(type.atlasKey)
            ?.also { LOG.debug { "Adding animation of type $type with ${it.size} regions" } }
            ?: run {
                LOG.error { "No regions found for ${type.atlasKey}" }
                findAtlasRegions(ERROR_REGION_NAME)
                    ?: throw GdxRuntimeException("There is no error region in the atlas")
            }

    private fun findAtlasRegions(type: String) =
        graphicAtlas.findRegions(type).takeIf { !it.isEmpty }

    override fun entityRemoved(entity: Entity) = Unit

    companion object {
        private val LOG = logger<AnimationSystem>()
        private const val ERROR_REGION_NAME = "error"
    }
}
