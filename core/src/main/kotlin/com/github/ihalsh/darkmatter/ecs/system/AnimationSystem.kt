package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.GdxRuntimeException
import com.github.ihalsh.darkmatter.ecs.component.Animation2D
import com.github.ihalsh.darkmatter.ecs.component.AnimationComponent
import com.github.ihalsh.darkmatter.ecs.component.AnimationType
import com.github.ihalsh.darkmatter.ecs.component.AnimationType.NONE
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.collections.isNotEmpty
import ktx.log.debug
import ktx.log.error
import ktx.log.logger
import java.util.*

private val LOG = logger<AnimationSystem>()

class AnimationSystem(private val atlas: TextureAtlas)
    : IteratingSystem(allOf(AnimationComponent::class, GraphicComponent::class).get()), EntityListener {
    private val animationCache = EnumMap<AnimationType, Animation2D>(AnimationType::class.java)

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(family, this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }

    override fun entityRemoved(entity: Entity) = Unit

    override fun entityAdded(entity: Entity) {
        entity[AnimationComponent.mapper]?.let { animationComponent ->
            animationComponent.run {
                animation = getAnimation(type)
                entity[GraphicComponent.mapper]?.setSpriteRegion(animation.getKeyFrame(stateTime))
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val animationComponent = entity[AnimationComponent.mapper]
        require(animationComponent != null) { "Entity |entity| must have an AnimationComponet. Entity $entity" }
        val graphic = entity[GraphicComponent.mapper]
        require(graphic != null) { "Entity |entity| must have an GraphicComponet. Entity $entity" }

        if (animationComponent.type == NONE)
            LOG.error { "No type specified for animation component $animationComponent for |entity| $entity" }
                    .also { return }
        if (animationComponent.type == animationComponent.animation.type) animationComponent.stateTime += deltaTime
        else {
            animationComponent.stateTime = 0f
            animationComponent.animation = getAnimation(animationComponent.type)
        }
        graphic.setSpriteRegion(animationComponent.animation.getKeyFrame(animationComponent.stateTime))
    }

    private fun getAnimation(type: AnimationType): Animation2D {
        animationCache[type]?.let { return it }
        val regions = atlas.findRegions(type.atlasKey).let {
            if (it.isNotEmpty()) it.also { LOG.debug { "Adding animation of type $type with ${it.size} regions" } }
            else when {
                atlas.findRegions("error").isEmpty -> throw GdxRuntimeException("There is no error region in the atlas")
                else -> atlas.findRegions("error").also { LOG.error { "No regions found for ${type.atlasKey}" } }
            }
        }
        return Animation2D(type, regions, type.playMode, type.speedRate).also { animationCache[type] = it }
    }

}