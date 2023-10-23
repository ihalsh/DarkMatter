package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.github.ihalsh.darkmatter.ecs.component.FacingComponent
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import com.github.ihalsh.darkmatter.asset.TextureAtlasAsset
import com.github.ihalsh.darkmatter.utils.facingComponent
import com.github.ihalsh.darkmatter.utils.graphicsComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.assets.async.AssetStorage

class PlayerAnimationSystem(asset: AssetStorage) : IteratingSystem(
    allOf(PlayerComponent::class, FacingComponent::class, GraphicComponent::class).get()
), EntityListener {
    private val graphicAtlas = asset[TextureAtlasAsset.GAME_GRAPHICS.descriptor]
    private val defaultRegion: TextureRegion = graphicAtlas.findRegion("ship_base")
    private val leftRegion: TextureRegion = graphicAtlas.findRegion("ship_left")
    private val rightRegion: TextureRegion = graphicAtlas.findRegion("ship_right")

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(family, this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }

    override fun entityAdded(entity: Entity) {
        entity[GraphicComponent.mapper]?.setSpriteRegion(defaultRegion)
    }

    override fun entityRemoved(entity: Entity) = Unit

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val facing = entity.facingComponent
        val graphics = entity.graphicsComponent

        // Return, if last direction is the same as current direction and the sprite's texture already set
        if (facing.direction == facing.previousDirection && graphics.sprite.texture != null) return

        facing.previousDirection = facing.direction

        val region = when (facing.direction) {
            FacingComponent.Direction.LEFT -> leftRegion
            FacingComponent.Direction.RIGHT -> rightRegion
            else -> defaultRegion
        }

        graphics.setSpriteRegion(region)
    }
}
